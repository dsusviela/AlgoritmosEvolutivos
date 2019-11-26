package org.uma.jmetal.operator.impl.mutation;

import java.util.Collection;
import java.util.Collections;
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
        int victimIndex, oldCellValue, oldCellPairIndex;

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
                    Collections.shuffle(victims);
                    victimIndex = victims.getFirst();

                    // Swap the cells and the pair references
                    oldCellValue = solution.getVariableValue(cellIndex);
                    oldCellPairIndex = solution.getVariableValue(cellIndex + 10);
                    solution.setVariableValue(cellIndex, solution.getVariableValue(victimIndex));
                    solution.setVariableValue(cellIndex + 10, solution.getVariableValue(victimIndex + 10));
                    solution.setVariableValue(oldCellPairIndex + 10, cellIndex);
                    solution.setVariableValue(victimIndex, oldCellValue);
                    solution.setVariableValue(victimIndex + 10, oldCellPairIndex);
                    solution.setVariableValue(solution.getVariableValue(victimIndex + 10), victimIndex);

                    // Only in turn mutation, the pairs are exchanged too
                    if (mutationType == 1) {
                        if (data.hasPair(cellIndex, solution)) {
                            solution = solveCollision(cellIndex, victimIndex - cellIndex, solution);
                        }
                        if (data.hasPair(victimIndex, solution)) {
                            solution = solveCollision(victimIndex, cellIndex - victimIndex, solution);
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
                    && solution.getVariableValue(victimIndex) != solution.getVariableValue(cellIndex);
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

    private IntegerSolution solveCollision(int cellIndex, int distance, IntegerSolution solution) {
        IntegerSolution altSolution = (IntegerSolution) solution.copy();
        int cellPairIndex = solution.getVariableValue(cellIndex + 10);
        int cellPairValue = solution.getVariableValue(cellPairIndex);

        // Solve collisions with the cell's pair
        if (solution.getVariableValue(cellPairIndex + distance) == -1) {
            // Swap the cells and the pair references, and the reference to me of my pair
            altSolution.setVariableValue(cellPairIndex + distance, cellPairValue);
            altSolution.setVariableValue(cellPairIndex + distance + 10, solution.getVariableValue(cellPairIndex + 10));
            altSolution.setVariableValue(cellIndex + 10, cellPairIndex + distance);
            altSolution.setVariableValue(cellPairIndex, 0);
            altSolution.setVariableValue(cellPairIndex + 10, 0);
        } else {
            // Save the current value in the conflicting cell
            int aux = solution.getVariableValue(cellPairIndex + distance);
            // Just override the current value with the pair value
            altSolution.setVariableValue(cellPairIndex + distance, cellPairValue);
            altSolution = data.findFeasibleClassroom(cellPairIndex, altSolution);
            if (altSolution == null) {
                altSolution = data.findFeasibleDay(cellPairIndex, altSolution);
                if (altSolution == null) {
                    //altSolution = data.findFeasibleDayAndClassroom(cellPairIndex, altSolution);
                    // findFeasibleDayAndClassroom leaves the parameter cell empty, so now I can put
                    // the original value again there
                    altSolution.setVariableValue(cellPairIndex + distance, aux);
                } else {
                    // findFeasibleDay leaves the parameter cell empty, so now I can put the
                    // original value again there
                    altSolution.setVariableValue(cellPairIndex + distance, aux);
                }
            } else {
                // findFeasibleClassroom leaves the parameter cell empty, so now I can put the
                // original value again there
                altSolution.setVariableValue(cellPairIndex + distance, aux);
            }
        }
        return altSolution;
    }
}
