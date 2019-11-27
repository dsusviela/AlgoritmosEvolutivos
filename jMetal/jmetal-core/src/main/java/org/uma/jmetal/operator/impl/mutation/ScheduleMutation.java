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
            if (randomGenerator.getRandomValue() <= probability && (cellIndex % 20 < 10) && !evaluated[cellIndex]) {
                if (solution.getLowerBound(cellIndex) == solution.getUpperBound(cellIndex)) {
                    solution.setVariableValue(cellIndex, solution.getLowerBound(cellIndex));
                } else {
                    int mutationType = (int) Math.floor(Math.random() * 3);

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
                    int victim = feasibleVictims.get(victimIndex);
                    cellHadPair = data.hasPair(cellIndex, solution);
                    victimHadPair = data.hasPair(victimIndex, solution);

                    // Swap the cells and the pair references
                    data.swapFeasibleClassroom(victim, cellIndex, solution);

                    // Only in turn mutation, the pairs are exchanged too
                    if (mutationType == 1) {
                        if (cellHadPair) {
                            if (!solveCollision(solution.getVariableValue(victimIndex + 10), solution)) {
                                // abort
                                solution = originalSolution;
                            }
                        }
                        if (victimHadPair) {
                            if (!solveCollision(solution.getVariableValue(cellIndex + 10), solution)) {
                                // abort
                                solution = originalSolution;
                            }
                        }
                    }
                }
            }
        }
    }

    LinkedList<Integer> dayMutation(IntegerSolution solution, int cellIndex) {
        LinkedList<Integer> res = new LinkedList<Integer>();
        int victimIndex = 0;
        boolean isSelectable = false;

        // Looks for a victim in the same column
        int startCellIndex = cellIndex - (cellIndex % 10);
        for (int k = 0; k < 10; k++) {
            victimIndex = startCellIndex + k;
            isSelectable = victimIndex != cellIndex && !evaluated[victimIndex]
                    && solution.getVariableValue(victimIndex) != solution.getVariableValue(cellIndex)
                    && data.isAvailable(victimIndex, solution) && data.getDay(cellIndex) != (k / 2);
            // The cell in the same block that the victim can't be my pair
            if (victimIndex % 2 == 0) {
                isSelectable &= solution.getVariableValue(victimIndex - 1) != solution.getVariableValue(cellIndex);
            } else {
                isSelectable &= solution.getVariableValue(victimIndex + 1) != solution.getVariableValue(cellIndex);
            }
            // The other cell in my block can't be the victim's pair
            if (cellIndex % 2 == 0) {
                isSelectable &= solution.getVariableValue(cellIndex - 1) != solution.getVariableValue(victimIndex);
            } else {
                isSelectable &= solution.getVariableValue(cellIndex + 1) != solution.getVariableValue(victimIndex);
            }
            if (isSelectable) {
                res.add(victimIndex);
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
        int turn = (int) Math.floor(cellIndex / 20) % 3;
        int startCellIndex = cellIndex - 20 * turn;
        if (cellIndex % 2 == 1) {
            startCellIndex -= 1;
        }
        for (int k = 0; k < 3; k++) {
            victimIndex = startCellIndex + 20 * k;
            isSelectable = victimIndex != cellIndex && !evaluated[victimIndex]
                    && solution.getVariableValue(victimIndex) != solution.getVariableValue(cellIndex)
                    && data.isAvailable(victimIndex, solution);
            // The cell in the same block that the victim can't be my pair
            if (victimIndex % 2 == 0) {
                isSelectable &= solution.getVariableValue(victimIndex - 1) != solution.getVariableValue(cellIndex);
            } else {
                isSelectable &= solution.getVariableValue(victimIndex + 1) != solution.getVariableValue(cellIndex);
            }
            // The other cell in my block can't be the victim's pair
            if (cellIndex % 2 == 0) {
                isSelectable &= solution.getVariableValue(cellIndex - 1) != solution.getVariableValue(victimIndex);
            } else {
                isSelectable &= solution.getVariableValue(cellIndex + 1) != solution.getVariableValue(victimIndex);
            }
            if (isSelectable) {
                res.add(victimIndex);
            }

            victimIndex += 1;
            isSelectable = victimIndex != cellIndex && !evaluated[victimIndex]
                    && solution.getVariableValue(victimIndex) != solution.getVariableValue(cellIndex);
            // The cell in the same block that the victim can't be my pair
            if (victimIndex % 2 == 0) {
                isSelectable &= solution.getVariableValue(victimIndex - 1) != solution.getVariableValue(cellIndex);
            } else {
                isSelectable &= solution.getVariableValue(victimIndex + 1) != solution.getVariableValue(cellIndex);
            }
            // The other cell in my block can't be the victim's pair
            if (cellIndex % 2 == 0) {
                isSelectable &= solution.getVariableValue(cellIndex - 1) != solution.getVariableValue(victimIndex);
            } else {
                isSelectable &= solution.getVariableValue(cellIndex + 1) != solution.getVariableValue(victimIndex);
            }
            if (isSelectable) {
                res.add(victimIndex);
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
        int classroom = (int) Math.floor(cellIndex / 60);
        int startCellIndex = cellIndex - classroom * 60;
        if (cellIndex % 2 == 1) {
            startCellIndex -= 1;
        }
        for (int k = 0; k < (int) Math.floor(solution.getNumberOfVariables() / 60); k++) {
            victimIndex = startCellIndex + 60 * k;
            isSelectable = victimIndex != cellIndex && !evaluated[victimIndex]
                    && solution.getVariableValue(victimIndex) != solution.getVariableValue(cellIndex)
                    && data.isAvailable(victimIndex, solution);
            if (victimIndex % 2 == 0) {
                isSelectable &= solution.getVariableValue(victimIndex - 1) != solution.getVariableValue(cellIndex);
            } else {
                isSelectable &= solution.getVariableValue(victimIndex + 1) != solution.getVariableValue(cellIndex);
            }
            if (isSelectable) {
                res.add(victimIndex);
            }

            victimIndex += 1;
            isSelectable = victimIndex != cellIndex && !evaluated[victimIndex]
                    && solution.getVariableValue(victimIndex) != solution.getVariableValue(cellIndex);
            if (victimIndex % 2 == 0) {
                isSelectable &= solution.getVariableValue(victimIndex - 1) != solution.getVariableValue(cellIndex);
            } else {
                isSelectable &= solution.getVariableValue(victimIndex + 1) != solution.getVariableValue(cellIndex);
            }
            if (isSelectable) {
                res.add(victimIndex);
            }
        }
        return res;
    }

    private boolean solveCollision(int cellIndex, IntegerSolution solution) {
        if (data.getAttendingStudents(solution.getVariableValue(cellIndex)) < data
                .getClassroomCapacity(data.getClassroom(cellIndex))) {
            return true;
        }

        int cellDestination = data.findFeasibleClassroom(cellIndex, solution);
        if (cellDestination == -1) {
            cellDestination = data.findFeasibleDay(cellIndex, solution);
            if (cellDestination == -1) {
                cellDestination = data.findFeasibleClassroomAndDay(cellIndex, solution);
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
