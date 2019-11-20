package org.uma.jmetal.operator.impl.mutation;

import java.util.LinkedList;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.IntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.solution.util.RepairDoubleSolution;
import org.uma.jmetal.solution.util.RepairDoubleSolutionAtBounds;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

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

    private double mutationProbability;
    private RepairDoubleSolution solutionRepair;

    private RandomGenerator<Double> randomGenerator;

    /** Constructor */
    public ScheduleMutation() {
        this(DEFAULT_PROBABILITY);
    }

    /** Constructor */
    public ScheduleMutation(IntegerProblem problem) {
        this(1.0 / problem.getNumberOfVariables());
    }

    /** Constructor */
    public ScheduleMutation(double mutationProbability) {
        this(mutationProbability, new RepairDoubleSolutionAtBounds());
    }

    /** Constructor */
    public ScheduleMutation(double mutationProbability, RepairDoubleSolution solutionRepair) {
        this(mutationProbability, solutionRepair, () -> JMetalRandom.getInstance().nextDouble());
    }

    /** Constructor */
    public ScheduleMutation(double mutationProbability, RepairDoubleSolution solutionRepair,
            RandomGenerator<Double> randomGenerator) {
        if (mutationProbability < 0) {
            throw new JMetalException("Mutation probability is negative: " + mutationProbability);
        }
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
        Double rnd, delta1, delta2, mutPow, deltaq;
        int cellValue, lowerBound, upperBound;
        boolean[] evaluated = new boolean[solution.getNumberOfVariables()];

        // For each cell
        for (int cell = 0; cell < solution.getNumberOfVariables(); cell++) {
            for (boolean item : evaluated) {
                item = false;
            }
            if (randomGenerator.getRandomValue() <= probability && (cell % 20 < 10) && !evaluated[cell]) {
                cellValue = solution.getVariableValue(cell);
                lowerBound = solution.getLowerBound(cell);
                upperBound = solution.getUpperBound(cell);

                if (lowerBound == upperBound) {
                    cellValue = lowerBound;
                } else {
                    // Find another victim in the same day and classroom
                    int victim = 0;
                    boolean isSelectable = false;
                    LinkedList<Integer> cells = new LinkedList<Integer>();
                    int mutationType = (int) Math.floor(Math.random() * 3);

                    // Day mutation
                    if (mutationType == 0) {
                        // Looks for a victim in the same column
                        int startCell = cell - (cell % 10);
                        for (int k = 0; k < 10; k++) {
                            victim = startCell + k;
                            isSelectable = victim != cell && !evaluated[victim]
                                    && solution.getVariableValue(victim) != solution.getVariableValue(cell);
                            // The cell in the same block that the victim can't be my pair
                            if (victim % 2 == 0) {
                                isSelectable &= solution.getVariableValue(victim - 1) != solution
                                        .getVariableValue(cell);
                            } else {
                                isSelectable &= solution.getVariableValue(victim + 1) != solution
                                        .getVariableValue(cell);
                            }
                            // The other cell in my block can't be the victim's pair
                            if (cell % 2 == 0) {
                                isSelectable &= solution.getVariableValue(cell - 1) != solution
                                        .getVariableValue(victim);
                            } else {
                                isSelectable &= solution.getVariableValue(cell + 1) != solution
                                        .getVariableValue(victim);
                            }
                            if (isSelectable) {
                                cells.add(victim);
                            }
                        }
                    }
                    // Turn mutation
                    else if (mutationType == 1) {
                        // Looks for a victim in the same day and classroom

                        // The turn of cell
                        int turn = (int) Math.floor(cell / 20) % 3;
                        int startCell = cell - 20 * turn;
                        if (cell % 2 == 1) {
                            startCell -= 1;
                        }
                        for (int k = 0; k < 3; k++) {
                            victim = startCell + 20 * k;
                            isSelectable = victim != cell && !evaluated[victim]
                                    && solution.getVariableValue(victim) != solution.getVariableValue(cell);
                            // The cell in the same block that the victim can't be my pair
                            if (victim % 2 == 0) {
                                isSelectable &= solution.getVariableValue(victim - 1) != solution
                                        .getVariableValue(cell);
                            } else {
                                isSelectable &= solution.getVariableValue(victim + 1) != solution
                                        .getVariableValue(cell);
                            }
                            // The other cell in my block can't be the victim's pair
                            if (cell % 2 == 0) {
                                isSelectable &= solution.getVariableValue(cell - 1) != solution
                                        .getVariableValue(victim);
                            } else {
                                isSelectable &= solution.getVariableValue(cell + 1) != solution
                                        .getVariableValue(victim);
                            }
                            if (isSelectable) {
                                cells.add(victim);
                            }

                            victim += 1;
                            isSelectable = victim != cell && !evaluated[victim]
                                    && solution.getVariableValue(victim) != solution.getVariableValue(cell);
                            // The cell in the same block that the victim can't be my pair
                            if (victim % 2 == 0) {
                                isSelectable &= solution.getVariableValue(victim - 1) != solution
                                        .getVariableValue(cell);
                            } else {
                                isSelectable &= solution.getVariableValue(victim + 1) != solution
                                        .getVariableValue(cell);
                            }
                            // The other cell in my block can't be the victim's pair
                            if (cell % 2 == 0) {
                                isSelectable &= solution.getVariableValue(cell - 1) != solution
                                        .getVariableValue(victim);
                            } else {
                                isSelectable &= solution.getVariableValue(cell + 1) != solution
                                        .getVariableValue(victim);
                            }
                            if (isSelectable) {
                                cells.add(victim);
                            }
                        }
                    }
                    // Classroom mutation
                    else {
                        // Looks for a victim in the same day and turn

                        // The classroom of cell
                        int classroom = (int) Math.floor(cell / 60);
                        int startCell = cell - classroom * 60;
                        if (cell % 2 == 1) {
                            startCell -= 1;
                        }
                        for (int k = 0; k < (int) Math.floor(solution.getNumberOfVariables() / 60); k++) {
                            victim = startCell + 60 * k;
                            isSelectable = victim != cell && !evaluated[victim]
                                    && solution.getVariableValue(victim) != solution.getVariableValue(cell);
                            if (victim % 2 == 0) {
                                isSelectable &= solution.getVariableValue(victim - 1) != solution
                                        .getVariableValue(cell);
                            } else {
                                isSelectable &= solution.getVariableValue(victim + 1) != solution
                                        .getVariableValue(cell);
                            }
                            if (isSelectable) {
                                cells.add(victim);
                            }

                            victim += 1;
                            isSelectable = victim != cell && !evaluated[victim]
                                    && solution.getVariableValue(victim) != solution.getVariableValue(cell);
                            if (victim % 2 == 0) {
                                isSelectable &= solution.getVariableValue(victim - 1) != solution
                                        .getVariableValue(cell);
                            } else {
                                isSelectable &= solution.getVariableValue(victim + 1) != solution
                                        .getVariableValue(cell);
                            }
                            if (isSelectable) {
                                cells.add(victim);
                            }
                        }
                    }
                }
                solution.setVariableValue(cell, (int) cellValue);
            }
        }
    }
}
