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
    private boolean[] evaluated;

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
        evaluated = new boolean[solution.getNumberOfVariables()];
        LinkedList<Integer> victims = new LinkedList<Integer>();

        // For each cell
        for (int cell = 0; cell < solution.getNumberOfVariables(); cell++) {
            for (boolean item : evaluated) {
                item = false;
            }
            victims.clear();
            if (randomGenerator.getRandomValue() <= probability && (cell % 20 < 10) && !evaluated[cell]) {
                if (solution.getLowerBound(cell) == solution.getUpperBound(cell)) {
                    solution.setVariableValue(cell, solution.getLowerBound(cell));
                } else {
                    int mutationType = (int) Math.floor(Math.random() * 3);

                    // Day mutation
                    if (mutationType == 0) {
                        victims = dayMutation(solution, cell);
                    }
                    // Turn mutation
                    else if (mutationType == 1) {
                        victims = turnMutation(solution, cell);
                    }
                    // Classroom mutation
                    else {
                        victims = classroomMutation(solution, cell);
                    }
                    Collections.shuffle(victims);

                    solution.setVariableValue(cell, solution.getVariableValue(victims.getFirst()));
                    solution.setVariableValue(victims.getFirst(), solution.getVariableValue(cell));

                    // Only in turn mutation, the pairs are exchanged too
                    if (mutationType == 1) {
                        int cellPair = solution.getVariableValue(cell + 10);
                        int cellPairValue = solution.getVariableValue(cellPair);
                        int victimPair = solution.getVariableValue(victims.getFirst() + 10);
                        int victimPairValue = solution.getVariableValue(victimPair);

                        // TODO: Resolver colisiones
                        solution.setVariableValue(cellPair, solution.getVariableValue(victimPair));
                        solution.setVariableValue(victims.getFirst(), solution.getVariableValue(cellPair));
                    }
                }
            }
        }
    }

    LinkedList<Integer> dayMutation(IntegerSolution solution, int cell) {
        LinkedList<Integer> res = new LinkedList<Integer>();
        int victim = 0;
        boolean isSelectable = false;

        // Looks for a victim in the same column
        int startCell = cell - (cell % 10);
        for (int k = 0; k < 10; k++) {
            victim = startCell + k;
            isSelectable = victim != cell && !evaluated[victim]
                    && solution.getVariableValue(victim) != solution.getVariableValue(cell);
            // The cell in the same block that the victim can't be my pair
            if (victim % 2 == 0) {
                isSelectable &= solution.getVariableValue(victim - 1) != solution.getVariableValue(cell);
            } else {
                isSelectable &= solution.getVariableValue(victim + 1) != solution.getVariableValue(cell);
            }
            // The other cell in my block can't be the victim's pair
            if (cell % 2 == 0) {
                isSelectable &= solution.getVariableValue(cell - 1) != solution.getVariableValue(victim);
            } else {
                isSelectable &= solution.getVariableValue(cell + 1) != solution.getVariableValue(victim);
            }
            if (isSelectable) {
                res.add(victim);
            }
        }
        return res;
    }

    LinkedList<Integer> turnMutation(IntegerSolution solution, int cell) {
        LinkedList<Integer> res = new LinkedList<Integer>();
        int victim = 0;
        boolean isSelectable = false;

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
                isSelectable &= solution.getVariableValue(victim - 1) != solution.getVariableValue(cell);
            } else {
                isSelectable &= solution.getVariableValue(victim + 1) != solution.getVariableValue(cell);
            }
            // The other cell in my block can't be the victim's pair
            if (cell % 2 == 0) {
                isSelectable &= solution.getVariableValue(cell - 1) != solution.getVariableValue(victim);
            } else {
                isSelectable &= solution.getVariableValue(cell + 1) != solution.getVariableValue(victim);
            }
            if (isSelectable) {
                res.add(victim);
            }

            victim += 1;
            isSelectable = victim != cell && !evaluated[victim]
                    && solution.getVariableValue(victim) != solution.getVariableValue(cell);
            // The cell in the same block that the victim can't be my pair
            if (victim % 2 == 0) {
                isSelectable &= solution.getVariableValue(victim - 1) != solution.getVariableValue(cell);
            } else {
                isSelectable &= solution.getVariableValue(victim + 1) != solution.getVariableValue(cell);
            }
            // The other cell in my block can't be the victim's pair
            if (cell % 2 == 0) {
                isSelectable &= solution.getVariableValue(cell - 1) != solution.getVariableValue(victim);
            } else {
                isSelectable &= solution.getVariableValue(cell + 1) != solution.getVariableValue(victim);
            }
            if (isSelectable) {
                res.add(victim);
            }
        }
        return res;
    }

    LinkedList<Integer> classroomMutation(IntegerSolution solution, int cell) {
        LinkedList<Integer> res = new LinkedList<Integer>();
        int victim = 0;
        boolean isSelectable = false;

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
                isSelectable &= solution.getVariableValue(victim - 1) != solution.getVariableValue(cell);
            } else {
                isSelectable &= solution.getVariableValue(victim + 1) != solution.getVariableValue(cell);
            }
            if (isSelectable) {
                res.add(victim);
            }

            victim += 1;
            isSelectable = victim != cell && !evaluated[victim]
                    && solution.getVariableValue(victim) != solution.getVariableValue(cell);
            if (victim % 2 == 0) {
                isSelectable &= solution.getVariableValue(victim - 1) != solution.getVariableValue(cell);
            } else {
                isSelectable &= solution.getVariableValue(victim + 1) != solution.getVariableValue(cell);
            }
            if (isSelectable) {
                res.add(victim);
            }
        }
        return res;
    }
}
