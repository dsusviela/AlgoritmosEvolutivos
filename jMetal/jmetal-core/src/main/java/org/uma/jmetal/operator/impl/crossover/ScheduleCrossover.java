package org.uma.jmetal.operator.impl.crossover;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.BoundedRandomGenerator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ScheduleCrossover implements CrossoverOperator<IntegerSolution> {
  private double crossoverProbability;
  private RandomGenerator<Double> crossoverRandomGenerator;
  private BoundedRandomGenerator<Integer> pointRandomGenerator;

  /** Constructor */
  public ScheduleCrossover(double crossoverProbability) {
    this(crossoverProbability, () -> JMetalRandom.getInstance().nextDouble(),
        (a, b) -> JMetalRandom.getInstance().nextInt(a, b));
  }

  /** Constructor */
  public ScheduleCrossover(double crossoverProbability, RandomGenerator<Double> randomGenerator) {
    this(crossoverProbability, randomGenerator, BoundedRandomGenerator.fromDoubleToInteger(randomGenerator));
  }

  /** Constructor */
  public ScheduleCrossover(double crossoverProbability, RandomGenerator<Double> crossoverRandomGenerator,
      BoundedRandomGenerator<Integer> pointRandomGenerator) {
    if (crossoverProbability < 0) {
      throw new JMetalException("Crossover probability is negative: " + crossoverProbability);
    }
    this.crossoverProbability = crossoverProbability;
    this.crossoverRandomGenerator = crossoverRandomGenerator;
    this.pointRandomGenerator = pointRandomGenerator;
  }

  @Override
  public int getNumberOfRequiredParents() {
    return 2;
  }

  @Override
  public int getNumberOfGeneratedChildren() {
    return 2;
  }

  @Override
  public List<IntegerSolution> execute(List<IntegerSolution> integerSolutions) {
    if (null == integerSolutions) {
      throw new JMetalException("Null parameter");
    } else if (integerSolutions.size() != 2) {
      throw new JMetalException("There must be two parents instead of " + integerSolutions.size());
    }

    return doCrossover(integerSolutions.get(0), integerSolutions.get(1));
  }

  private List<IntegerSolution> doCrossover(IntegerSolution parent1, IntegerSolution parent2) {
    if (crossoverRandomGenerator.getRandomValue() <= crossoverProbability) {
      int crossoverType = (int) Math.floor(Math.random() * 3);

      if (crossoverType == 0) {
        return turnCrossover(parent1, parent2);
      } else if (crossoverType == 1) {
        return dayCrossover(parent1, parent2);
      } else {
        return classroomCrossover(parent1, parent2);
      }
    } else {
      List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
      offspring.add((IntegerSolution) parent1.copy());
      offspring.add((IntegerSolution) parent2.copy());
      return offspring;
    }
  }

  private List<IntegerSolution> turnCrossover(IntegerSolution parent1, IntegerSolution parent2) {
    IntegerSolution child1 = (IntegerSolution) parent1.copy();
    IntegerSolution child2 = (IntegerSolution) parent2.copy();

    // gets a random turn
    Random rand = new Random();
    // starts at the beginning of a random turn and classroom
    int start = (int) (Math.floor(rand.nextInt(child1.getNumberOfVariables() + 1) / 20) * 20);

    // data transfer
    for (int idx = start; idx < start + 20; idx++) {
      // transfering the info
      if (idx < 10) {
        child1.setVariableValue(idx, parent2.getVariableValue(idx));
        child2.setVariableValue(idx, parent1.getVariableValue(idx));
      }
      // transfering the pairs
      else {
        // TODO: Resolver colisiones
        child1.setVariableValue(idx, parent2.getVariableValue(parent2.getVariableValue(idx)));
        child2.setVariableValue(idx, parent1.getVariableValue(parent1.getVariableValue(idx)));
      }
    }

    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
    offspring.add(child1);
    offspring.add(child2);
    return offspring;
  }

  private List<IntegerSolution> dayCrossover(IntegerSolution parent1, IntegerSolution parent2) {
    IntegerSolution child1 = (IntegerSolution) parent1.copy();
    IntegerSolution child2 = (IntegerSolution) parent2.copy();

    // gets a random day
    Random rand = new Random();
    // starts at the beginning of a random day
    int start = (int) Math.floor(rand.nextInt(5));

    // data transfer
    for (int idx = start * 2 + 1; idx < child1.getNumberOfVariables(); idx += 20) {
      // transfering the info
      child1.setVariableValue(idx, parent2.getVariableValue(idx));
      child2.setVariableValue(idx, parent1.getVariableValue(idx));
      // transfering pairs
      // TODO: Resolver colisiones
      child1.setVariableValue(idx, parent2.getVariableValue(parent2.getVariableValue(idx + 10)));
      child2.setVariableValue(idx, parent1.getVariableValue(parent1.getVariableValue(idx + 10)));
    }

    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
    offspring.add(child1);
    offspring.add(child2);
    return offspring;
  }

  private List<IntegerSolution> classroomCrossover(IntegerSolution parent1, IntegerSolution parent2) {
    IntegerSolution child1 = (IntegerSolution) parent1.copy();
    IntegerSolution child2 = (IntegerSolution) parent2.copy();

    // gets a random day
    Random rand = new Random();
    // starts at the beginning of a random classroom
    int start = (int) (Math.floor(rand.nextInt(child1.getNumberOfVariables() + 1) / 60) * 60);

    // data transfer
    for (int idx = start; idx < child1.getNumberOfVariables(); idx++) {
      // transfering the info
      if ((idx / 20) % 2 == 0) {
        child1.setVariableValue(idx, parent2.getVariableValue(idx));
        child2.setVariableValue(idx, parent1.getVariableValue(idx));
      }
      // transfering the pairs
      else {
        // TODO: Resolver colisiones
        child1.setVariableValue(idx, parent2.getVariableValue(parent2.getVariableValue(idx)));
        child2.setVariableValue(idx, parent1.getVariableValue(parent1.getVariableValue(idx)));
      }
    }

    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
    offspring.add(child1);
    offspring.add(child2);
    return offspring;
  }
}