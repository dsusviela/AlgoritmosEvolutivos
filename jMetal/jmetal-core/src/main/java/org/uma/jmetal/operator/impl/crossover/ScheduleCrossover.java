package org.uma.jmetal.operator.impl.crossover;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.BoundedRandomGenerator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;
import org.uma.jmetal.util.scheduledata.ScheduleDataHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ScheduleCrossover implements CrossoverOperator<IntegerSolution> {
  private double crossoverProbability;
  private RandomGenerator<Double> crossoverRandomGenerator;
  private BoundedRandomGenerator<Integer> pointRandomGenerator;
  private ScheduleDataHandler data;

  /** Constructor */
  public ScheduleCrossover(ScheduleDataHandler data, double crossoverProbability) {
    this(data, crossoverProbability, () -> JMetalRandom.getInstance().nextDouble(),
        (a, b) -> JMetalRandom.getInstance().nextInt(a, b));
  }

  /** Constructor */
  public ScheduleCrossover(ScheduleDataHandler data, double crossoverProbability,
      RandomGenerator<Double> randomGenerator) {
    this(data, crossoverProbability, randomGenerator, BoundedRandomGenerator.fromDoubleToInteger(randomGenerator));
  }

  /** Constructor */
  public ScheduleCrossover(ScheduleDataHandler data, double crossoverProbability,
      RandomGenerator<Double> crossoverRandomGenerator, BoundedRandomGenerator<Integer> pointRandomGenerator) {
    if (crossoverProbability < 0) {
      throw new JMetalException("Crossover probability is negative: " + crossoverProbability);
    }
    this.crossoverProbability = crossoverProbability;
    this.crossoverRandomGenerator = crossoverRandomGenerator;
    this.pointRandomGenerator = pointRandomGenerator;
    this.data = data;
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
    int oldCellValue, oldCellPairIndex;

    // gets a random turn
    Random rand = new Random();
    // starts at the beginning of a random turn and classroom
    int start = (int) (Math.floor(rand.nextInt(child1.getNumberOfVariables() + 1) / 20) * 20);

    // Cells exchange
    for (int cellIndex = start; cellIndex < start + 10; cellIndex++) {
      // Swap the cells and the pair references
      // Parent 1 -> Child 2
      oldCellValue = parent1.getVariableValue(cellIndex);
      oldCellPairIndex = parent1.getVariableValue(cellIndex + 10);
      child2.setVariableValue(cellIndex, oldCellValue);
      // The reference to the pair is subject to change
      child2.setVariableValue(cellIndex + 10, oldCellPairIndex);

      // Parent 2 -> Child 1
      oldCellValue = parent2.getVariableValue(cellIndex);
      oldCellPairIndex = parent2.getVariableValue(cellIndex + 10);
      child1.setVariableValue(cellIndex, oldCellValue);
      // The reference to the pair is subject to change
      child1.setVariableValue(cellIndex + 10, oldCellPairIndex);
    }

    // Pairs exchange
    for (int cellIndex = start; cellIndex < start + 10; cellIndex++) {
      // Child 1
      oldCellPairIndex = child1.getVariableValue(cellIndex + 10);
      // If the pair was not already swapped
      if (!(oldCellPairIndex >= start && oldCellPairIndex < start + 10)) {
        child1 = solveCollision(oldCellPairIndex, child1, parent1, parent2);
      }

      // Child 2
      oldCellPairIndex = child2.getVariableValue(cellIndex + 10);
      // If the pair was not already swapped
      if (!(oldCellPairIndex >= start && oldCellPairIndex < start + 10)) {
        child2 = solveCollision(oldCellPairIndex, child2, parent2, parent1);
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
    int oldCellValue, oldCellPairIndex;

    // gets a random day
    Random rand = new Random();
    // starts at the beginning of a random day
    int start = (int) Math.floor(rand.nextInt(5));

    // Cells exchange
    for (int cellIndex = start * 2; cellIndex < child1.getNumberOfVariables(); cellIndex += 20) {
      // First cell of day

      // Swap the cells and the pair references
      // Parent 1 -> Child 2
      oldCellValue = parent1.getVariableValue(cellIndex);
      oldCellPairIndex = parent1.getVariableValue(cellIndex + 10);
      child2.setVariableValue(cellIndex, oldCellValue);
      // The reference to the pair is subject to change
      child2.setVariableValue(cellIndex + 10, oldCellPairIndex);

      // Parent 2 -> Child 1
      oldCellValue = parent2.getVariableValue(cellIndex);
      oldCellPairIndex = parent2.getVariableValue(cellIndex + 10);
      child1.setVariableValue(cellIndex, oldCellValue);
      // The reference to the pair is subject to change
      child1.setVariableValue(cellIndex + 10, oldCellPairIndex);

      // Second cell of day

      // Swap the cells and the pair references
      // Parent 1 -> Child 2
      oldCellValue = parent1.getVariableValue(cellIndex + 1);
      oldCellPairIndex = parent1.getVariableValue(cellIndex + 11);
      child2.setVariableValue(cellIndex + 1, oldCellValue);
      // The reference to the pair is subject to change
      child2.setVariableValue(cellIndex + 11, oldCellPairIndex);

      // Parent 2 -> Child 1
      oldCellValue = parent2.getVariableValue(cellIndex + 1);
      oldCellPairIndex = parent2.getVariableValue(cellIndex + 11);
      child1.setVariableValue(cellIndex + 1, oldCellValue);
      // The reference to the pair is subject to change
      child1.setVariableValue(cellIndex + 11, oldCellPairIndex);
    }

    // Pairs exchange
    for (int cellIndex = start * 2; cellIndex < child1.getNumberOfVariables(); cellIndex += 20) {
      // First cell of day

      // Child 1
      oldCellPairIndex = child1.getVariableValue(cellIndex + 10);
      // If the pair was not already swapped
      if (data.getDay(oldCellPairIndex) != data.getDay(cellIndex)) {
        child1 = solveCollision(oldCellPairIndex, child1, parent1, parent2);
      }

      // Child 2
      oldCellPairIndex = child2.getVariableValue(cellIndex + 10);
      // If the pair was not already swapped
      if (data.getDay(oldCellPairIndex) != data.getDay(cellIndex)) {
        child2 = solveCollision(oldCellPairIndex, child2, parent2, parent1);
      }

      // Second cell of day

      // Child 1
      oldCellPairIndex = child1.getVariableValue(cellIndex + 11);
      // If the pair was not already swapped
      if (data.getDay(oldCellPairIndex) != data.getDay(cellIndex)) {
        child1 = solveCollision(oldCellPairIndex, child1, parent1, parent2);
      }

      // Child 2
      oldCellPairIndex = child2.getVariableValue(cellIndex + 11);
      // If the pair was not already swapped
      if (data.getDay(oldCellPairIndex) != data.getDay(cellIndex)) {
        child2 = solveCollision(oldCellPairIndex, child2, parent2, parent1);
      }
    }

    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
    offspring.add(child1);
    offspring.add(child2);
    return offspring;
  }

  private List<IntegerSolution> classroomCrossover(IntegerSolution parent1, IntegerSolution parent2) {
    IntegerSolution child1 = (IntegerSolution) parent1.copy();
    IntegerSolution child2 = (IntegerSolution) parent2.copy();
    int oldCellValue, oldCellPairIndex;

    // gets a random classroom
    Random rand = new Random();
    // starts at the beginning of a random classroom
    int start = (int) (Math.floor(rand.nextInt(child1.getNumberOfVariables() + 1) / 60) * 60);

    // Cells exchange
    for (int cellIndex = start; cellIndex < start + 60; cellIndex++) {
      if (cellIndex % 20 < 10) {
        // Swap the cells and the pair references
        // Parent 1 -> Child 2
        oldCellValue = parent1.getVariableValue(cellIndex);
        oldCellPairIndex = parent1.getVariableValue(cellIndex + 10);
        child2.setVariableValue(cellIndex, oldCellValue);
        // The reference to the pair is subject to change
        child2.setVariableValue(cellIndex + 10, oldCellPairIndex);

        // Parent 2 -> Child 1
        oldCellValue = parent2.getVariableValue(cellIndex);
        oldCellPairIndex = parent2.getVariableValue(cellIndex + 10);
        child1.setVariableValue(cellIndex, oldCellValue);
        // The reference to the pair is subject to change
        child1.setVariableValue(cellIndex + 10, oldCellPairIndex);
      }
    }

    // Pairs exchange
    for (int cellIndex = start; cellIndex < start + 60; cellIndex++) {
      if (cellIndex % 20 < 10) {
        // Child 1
        oldCellPairIndex = child1.getVariableValue(cellIndex + 10);
        // If the pair was not already swapped
        if (data.getClassroom(oldCellPairIndex) != data.getClassroom(cellIndex)) {
          child1 = solveCollision(oldCellPairIndex, child1, parent1, parent2);
        }

        // Child 2
        oldCellPairIndex = child2.getVariableValue(cellIndex + 10);
        // If the pair was not already swapped
        if (data.getClassroom(oldCellPairIndex) != data.getClassroom(cellIndex)) {
          child2 = solveCollision(oldCellPairIndex, child2, parent2, parent1);
        }
      }
    }

    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
    offspring.add(child1);
    offspring.add(child2);
    return offspring;
  }

  IntegerSolution solveCollision(int cellIndex, IntegerSolution child, IntegerSolution originalParent,
      IntegerSolution otherParent) {
    IntegerSolution altSolution = (IntegerSolution) child.copy();

    if (child.getVariableValue(cellIndex) == -1) {
      child.setVariableValue(cellIndex, otherParent.getVariableValue(cellIndex));
    } else {
      // The current value of the conflicting cell is saved in the parent, so
      // just override the current value with the pair value
      child.setVariableValue(cellIndex, otherParent.getVariableValue(cellIndex));
      altSolution = data.findFeasibleClassroom(cellIndex, child);
      if (altSolution == null) {
        altSolution = data.findFeasibleDay(cellIndex, child);
        if (altSolution == null) {
          //child = data.findFeasibleDayAndClassroom(cellIndex, child);
          // findFeasibleDayAndClassroom leaves the parameter cell empty, so now I can put
          // the original value again there
          child.setVariableValue(cellIndex, originalParent.getVariableValue(cellIndex));
        } else {
          child = altSolution;
          // findFeasibleDay leaves the parameter cell empty, so now I can put the
          // original value again there
          child.setVariableValue(cellIndex, originalParent.getVariableValue(cellIndex));
        }
      } else {
        child = altSolution;
        // findFeasibleClassroom leaves the parameter cell empty, so now I can put the
        // original value again there
        child.setVariableValue(cellIndex, originalParent.getVariableValue(cellIndex));
      }
    }
    return altSolution;
  }
}