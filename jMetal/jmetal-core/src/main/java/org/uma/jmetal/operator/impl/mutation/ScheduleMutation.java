package org.uma.jmetal.operator.impl.mutation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.IntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.solution.util.RepairDoubleSolution;
import org.uma.jmetal.solution.util.RepairDoubleSolutionAtBounds;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;
//import org.uma.jmetal.problem.singleobjective.Schedule;
import org.uma.jmetal.util.scheduledata.ScheduleDataHandler;

/**
 * This class implements a polynomial mutation operator to be applied to Integer
 * solutions
 *
 * If the lower and upper bounds of a variable are the same, no mutation is
 * carried out and the bound value is returned.
 *
 * A {@link RepairDoubleSolution} object is used to decide the strategy to apply
 * when a value is out of range.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class ScheduleMutation implements MutationOperator<IntegerSolution> {
    private static final double DEFAULT_PROBABILITY = 0.01;

    // private Schedule schedule;
    private double mutationProbability;
    private RepairDoubleSolution solutionRepair;
    private RandomGenerator<Double> randomGenerator;
    private boolean[] evaluated;
    private ScheduleDataHandler data;

    /** Constructor */
    public ScheduleMutation(ScheduleDataHandler data) {
        this(data, DEFAULT_PROBABILITY);
    }

    /** Constructor */
    public ScheduleMutation(ScheduleDataHandler data, IntegerProblem problem) {
        this(data, 1.0 / problem.getNumberOfVariables());
    }

    /** Constructor */
    public ScheduleMutation(ScheduleDataHandler data, double mutationProbability) {
        this(data, mutationProbability, new RepairDoubleSolutionAtBounds());
    }

    /** Constructor */
    public ScheduleMutation(ScheduleDataHandler data, double mutationProbability, RepairDoubleSolution solutionRepair) {
        this(data, mutationProbability, solutionRepair, () -> JMetalRandom.getInstance().nextDouble());
    }

    /** Constructor */
    public ScheduleMutation(ScheduleDataHandler data, double mutationProbability, RepairDoubleSolution solutionRepair,
            RandomGenerator<Double> randomGenerator) {
        if (mutationProbability < 0) {
            throw new JMetalException("Mutation probability is negative: " + mutationProbability);
        }
        this.data = data;
        this.mutationProbability = mutationProbability;
        this.solutionRepair = solutionRepair;
        this.randomGenerator = randomGenerator;
    }

    /* Getters */
    public double getMutationProbability() {
        return mutationProbability;
    }

    /* Setters */
    public void setMutationProbability(double mutationProbability) {
        this.mutationProbability = mutationProbability;
    }

    /** Execute() method */
    public IntegerSolution execute(IntegerSolution solution) throws JMetalException {
        if (null == solution) {
            throw new JMetalException("Null parameter");
        }

        System.out.println("EMPIEZO MUTACION");

        doMutation(mutationProbability, solution);
        return solution;
    }

    /** Perform the mutation operation */
    private void doMutation(double probability, IntegerSolution solution) {
        evaluated = new boolean[solution.getNumberOfVariables()];
        LinkedList<Integer> victims = new LinkedList<Integer>();
        int victimIndex;
        boolean cellHadPair, victimHadPair;
        IntegerSolution originalSolution = (IntegerSolution) solution.copy();

        // For each cell
        for (int cellIndex = 0; cellIndex < solution.getNumberOfVariables(); cellIndex++) {
            for (boolean item : evaluated) {
                item = false;
            }
            victims.clear();
            if (randomGenerator.getRandomValue() <= probability && data.isIndexClass(cellIndex)
                    && !data.isAvailable(cellIndex, originalSolution) && !evaluated[cellIndex]) {
                System.out.println("MUTANDO...");
                if (solution.getLowerBound(cellIndex) == solution.getUpperBound(cellIndex)) {
                    solution.setVariableValue(cellIndex, solution.getLowerBound(cellIndex));
                } else {
                    int mutationType = JMetalRandom.getInstance().nextInt(0, 2);

                    // Day mutation
                    if (mutationType == 0) {
                        victims = dayMutation(solution, cellIndex);
                    }
                    // Turn mutation
                    else if (mutationType == 1) {
                        victims = turnMutation(solution, cellIndex);
                    }
                    // Classroom mutation
                    else {
                        victims = classroomMutation(solution, cellIndex);
                    }
                    LinkedList<Integer> feasibleVictims = new LinkedList<Integer>();
                    int attendingStudents = data.getAttendingStudents(solution.getVariableValue(cellIndex));
                    int indexCapacity = data.getClassroomCapacity(data.getClassroom(cellIndex));
                    for (Integer victim : victims) {
                        int vicitimCapacity = data.getClassroomCapacity(data.getClassroom(cellIndex));
                        int victimAttendingStudents = data.getAttendingStudents(solution.getVariableValue(victim));
                        if (attendingStudents < vicitimCapacity && victimAttendingStudents < indexCapacity) {
                            feasibleVictims.add(victim);
                        }
                    }
                    if (feasibleVictims.isEmpty()) {
                        break;
                    }
                    Collections.shuffle(feasibleVictims);
                    victimIndex = feasibleVictims.getFirst();
                    cellHadPair = data.hasPair(cellIndex, solution);
                    victimHadPair = data.hasPair(victimIndex, solution);

                    // Only in turn mutation, the pairs are exchanged too
                    if (mutationType == 1) {
                        // Swap the cells and the pair references
                        solution = data.swapFeasibleClassroom(victimIndex, cellIndex, solution);

                        int cellPairIndex = solution.getVariableValue(cellIndex + 10);
                        int victimPairIndex = solution.getVariableValue(victimIndex + 10);

                        if (cellHadPair && victimHadPair) {
                            data.unsafeSwap(cellPairIndex, victimPairIndex, solution);
                            if (!solveCollision(cellPairIndex, solution)) {
                                // abort
                                System.out.println("MUTATION ABORTED");
                                solution = originalSolution;
                            }
                            if (!solveCollision(victimPairIndex, solution)) {
                                // abort
                                System.out.println("MUTATION ABORTED");
                                solution = originalSolution;
                            }
                        }

                        if (cellHadPair && !victimHadPair) {
                            int cellToSubstitute = 20 * data.getTurn(victimIndex) + 2 * data.getDay(cellPairIndex);
                            int cellToSubstituteValue = solution.getVariableValue(cellToSubstitute);
                            int cellToSubstituteRefValue = solution.getVariableValue(cellToSubstitute + 10);

                            solution.setVariableValue(cellToSubstitute, solution.getVariableValue(cellPairIndex));
                            solution.setVariableValue(cellToSubstitute + 10, victimIndex);

                            if (!solveCollision(cellToSubstitute, solution)) {
                                // abort
                                System.out.println("MUTATION ABORTED");
                                solution = originalSolution;
                            } else {
                                solution.setVariableValue(cellToSubstitute, cellToSubstituteValue);
                                solution.setVariableValue(cellToSubstitute + 10, cellToSubstituteRefValue);
                            }
                        }

                        if (victimHadPair && !cellHadPair) {
                            int cellToSubstitute = 20 * data.getTurn(cellIndex) + 2 * data.getDay(victimPairIndex);
                            int cellToSubstituteValue = solution.getVariableValue(cellToSubstitute);
                            int cellToSubstituteRefValue = solution.getVariableValue(cellToSubstitute + 10);

                            solution.setVariableValue(cellToSubstitute, solution.getVariableValue(victimPairIndex));
                            solution.setVariableValue(cellToSubstitute + 10, cellIndex);

                            if (!solveCollision(cellToSubstitute, solution)) {
                                // abort
                                System.out.println("MUTATION ABORTED");
                                solution = originalSolution;
                            } else {
                                solution.setVariableValue(cellToSubstitute, cellToSubstituteValue);
                                solution.setVariableValue(cellToSubstitute + 10, cellToSubstituteRefValue);
                            }
                        }
                    } else {
                        int oldCellValue = solution.getVariableValue(cellIndex);
                        solution.setVariableValue(cellIndex, solution.getVariableValue(victimIndex));
                        solution.setVariableValue(victimIndex, oldCellValue);
                        oldCellValue = solution.getVariableValue(cellIndex + 10);
                        solution.setVariableValue(cellIndex + 10, solution.getVariableValue(victimIndex + 10));
                        solution.setVariableValue(victimIndex + 10, solution.getVariableValue(cellIndex + 10));
                    }
                }
            }
        }
    }

    LinkedList<Integer> dayMutation(IntegerSolution solution, int cellIndex) {
        LinkedList<Integer> res = new LinkedList<Integer>();
        int victimIndex = 0;
        boolean isSelectable = false;

        int cellDay = data.getDay(cellIndex);
        // Looks for a victim in the same column
        for (int day = 0; day < 5; day++) {
            if (cellDay == day) {
                continue;
            }
            for (int cell = 0; cell < 2; cell++) {
                victimIndex = 60 * data.getClassroom(cellIndex) + 20 * data.getTurn(cellIndex) + 2 * day + cell;
                isSelectable = !evaluated[victimIndex] && !data.isAvailable(victimIndex, solution)
                        && solution.getVariableValue(victimIndex) != solution.getVariableValue(cellIndex);
                // The cell in the same block that the victim can't be my pair
                if (victimIndex % 2 == 0) {
                    isSelectable &= victimIndex - 1 != solution.getVariableValue(cellIndex + 10);
                } else {
                    isSelectable &= victimIndex + 1 != solution.getVariableValue(cellIndex + 10);
                }
                if (isSelectable) {
                    res.add(victimIndex);
                }
            }
        }
        return res;
    }

    LinkedList<Integer> turnMutation(IntegerSolution solution, int cellIndex) {
        LinkedList<Integer> res = new LinkedList<Integer>();
        int victimIndex = 0;
        boolean isSelectable = false;

        // Looks for a victim in the same day and classroom

        // The turn of cell
        int cellTurn = data.getTurn(cellIndex);

        for (int turn = 0; turn < 3; turn++) {
            if (cellTurn == turn) {
                continue;
            }
            for (int cell = 0; cell < 2; cell++) {
                victimIndex = 60 * data.getClassroom(cellIndex) + 20 * turn + 2 * data.getDay(cellIndex) + cell;
                isSelectable = !evaluated[victimIndex] && !data.isAvailable(victimIndex, solution)
                        && solution.getVariableValue(victimIndex) != solution.getVariableValue(cellIndex);
                // The other cell in my block can't be the victim's pair
                if (victimIndex % 2 == 0) {
                    isSelectable &= victimIndex - 1 != solution.getVariableValue(cellIndex + 10);
                } else {
                    isSelectable &= victimIndex + 1 != solution.getVariableValue(cellIndex + 10);
                }
                if (isSelectable) {
                    res.add(victimIndex);
                }
            }
        }
        return res;
    }

    LinkedList<Integer> classroomMutation(IntegerSolution solution, int cellIndex) {
        LinkedList<Integer> res = new LinkedList<Integer>();
        int victimIndex = 0;
        boolean isSelectable = false;

        // Looks for a victim in the same day and turn

        // The classroom of cell
        int cellClassroom = data.getClassroom(cellIndex);
        for (int candidateClassroom = 0; candidateClassroom < data.getClassroomQty(); candidateClassroom++) {
            if (cellClassroom == candidateClassroom) {
                continue;
            }
            for (int cell = 0; cell < 2; cell++) {
                victimIndex = 60 * candidateClassroom + 20 * data.getTurn(cellIndex) + 2 * data.getDay(cellIndex)
                        + cell;
                isSelectable = !evaluated[victimIndex] && !data.isAvailable(victimIndex, solution)
                        && solution.getVariableValue(victimIndex) != solution.getVariableValue(cellIndex);
                if (victimIndex % 2 == 0) {
                    isSelectable &= victimIndex - 1 != solution.getVariableValue(cellIndex + 10);
                } else {
                    isSelectable &= victimIndex + 1 != solution.getVariableValue(cellIndex + 10);
                }
                if (isSelectable) {
                    res.add(victimIndex);
                }
            }
        }
        return res;
    }

    private boolean solveCollision(int cellIndex, IntegerSolution solution) {
        if (data.getAttendingStudents(solution.getVariableValue(cellIndex)) < data
                .getClassroomCapacity(data.getClassroom(cellIndex))) {
            return true;
        }

        int cellDestination = data.findFeasibleClassroom(cellIndex, solution, false);
        if (cellDestination == -1) {
            cellDestination = data.findFeasibleDay(cellIndex, solution, false);
            if (cellDestination == -1) {
                cellDestination = data.findFeasibleClassroomAndDay(cellIndex, solution, false);
                if (cellDestination == -1) {
                    // Harakiri
                    return false;
                }
            }
        }

        int cellPairIndex = solution.getVariableValue(cellIndex + 10);

        // Swap the cells and the pair references, and the reference to me of my pair
        solution.setVariableValue(cellDestination, solution.getVariableValue(cellIndex));
        solution.setVariableValue(cellDestination + 10, cellPairIndex);
        solution.setVariableValue(cellIndex, ScheduleDataHandler.AVAILABLE_INDEX);
        solution.setVariableValue(cellIndex + 10, ScheduleDataHandler.AVAILABLE_INDEX);
        solution.setVariableValue(cellPairIndex + 10, cellDestination);

        return true;
    }
}
