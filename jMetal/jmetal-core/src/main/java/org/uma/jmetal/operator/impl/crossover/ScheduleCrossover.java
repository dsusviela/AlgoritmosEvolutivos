package org.uma.jmetal.operator.impl.crossover;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.BoundedRandomGenerator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;
import org.uma.jmetal.util.scheduledata.ScheduleDataHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
        return dayCrossover(parent1, parent2);
      } else if (crossoverType == 1) {
        return turnCrossover(parent1, parent2);
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

  private List<IntegerSolution> dayCrossover(IntegerSolution parent1, IntegerSolution parent2) {
    Random random = new Random();
    int day = random.nextInt(5);
    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
    offspring.add(doDayCrossover(parent1, parent2, day));
    offspring.add(doDayCrossover(parent2, parent1, day));
    return offspring;
  }

  private IntegerSolution doDayCrossover(IntegerSolution father, IntegerSolution mother, int day) {
    IntegerSolution child = (IntegerSolution) mother.copy();

    // register classes to delete from mother
    HashMap<Integer, Integer> classBalance = new HashMap<Integer, Integer>();
    for (int turn = 0; turn < 3; turn++) {
      for (int classroom = 0; classroom < data.getClassroomCapacity().keySet().size(); classroom++) {
        for (int cell = 0; cell < 2; cell++) {
          int cellIndex = classroom * 60 + turn * 20 + day * 2 + cell;
          if (!data.isAvailable(cellIndex, mother)) {
            // we register the loss in the child
            int amount = (classBalance.containsKey(mother.getVariableValue(cellIndex))
                ? classBalance.get(mother.getVariableValue(cellIndex))
                : 0);
            amount--;
            classBalance.put(mother.getVariableValue(cellIndex), amount);
            // delete step is done here
            child.setVariableValue(cellIndex, ScheduleDataHandler.AVAILABLE_INDEX);
            child.setVariableValue(cellIndex + 10, ScheduleDataHandler.AVAILABLE_INDEX);
            if (data.hasPair(cellIndex, mother)) {
              child.setVariableValue(mother.getVariableValue(cellIndex + 10), ScheduleDataHandler.AVAILABLE_INDEX);
              child.setVariableValue(mother.getVariableValue(cellIndex + 10) + 10, ScheduleDataHandler.AVAILABLE_INDEX);
            }
            // register what we're about to insert
            amount = (classBalance.containsKey(father.getVariableValue(cellIndex))
                ? classBalance.get(father.getVariableValue(cellIndex))
                : 0);
            amount++;
            classBalance.put(father.getVariableValue(cellIndex), amount);
          }
        }
      }
    }

    // first three steps are done for the child
    // we now neeed to delete the excess
    for (int cellIndex = 0; cellIndex < data.getCellsInMatrix(); cellIndex++) {
      if (!data.isIndexClass(cellIndex) || data.isAvailable(cellIndex, child)) {
        continue;
      }
      int classWithType = child.getVariableValue(cellIndex);
      if (classBalance.keySet().contains(classWithType) && 0 < classBalance.get(classWithType)) {
        child.setVariableValue(cellIndex, ScheduleDataHandler.AVAILABLE_INDEX);
        child.setVariableValue(cellIndex + 10, ScheduleDataHandler.AVAILABLE_INDEX);
        if (data.hasPair(cellIndex, mother)) {
          child.setVariableValue(mother.getVariableValue(cellIndex + 10), ScheduleDataHandler.AVAILABLE_INDEX);
          child.setVariableValue(mother.getVariableValue(cellIndex + 10) + 10, ScheduleDataHandler.AVAILABLE_INDEX);
        }
        int amount = classBalance.get(classWithType) - 1;
        classBalance.put(classWithType, amount);
      }
    }

    // we need to insert now
    for (int classroom = 0; classroom < data.getClassroomCapacity().keySet().size(); classroom++) {
      for (int turn = 0; turn < 3; turn++) {
        for (int cell = 0; cell < 2; cell++) {
          int cellIndex = 60 * classroom + 20 * turn + 2 * day + cell;
          child.setVariableValue(cellIndex, father.getVariableValue(cellIndex));
          child.setVariableValue(cellIndex + 10, father.getVariableValue(cellIndex + 10));
          solveCollision(father.getVariableValue(cellIndex + 10), child, father);
        }
      }
    }

    // if the balance is negative, add the missing classes
    for (int classWithType = 0; classWithType < data.getAmountCourses() * 4; classWithType++) {
      if (classBalance.containsKey(classWithType)) {
        while (classBalance.get(classWithType) < 0) {
          data.insertClassIntoSolution(classWithType, child);
        }
      }
    }

    return child;
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

  boolean solveCollision(int cellIndex, IntegerSolution child, IntegerSolution father) {
    int cellDestination = cellIndex;

    if (!data.isAvailable(cellIndex, child) && !data.isAvailable(data.getNeighbourIndex(cellIndex), child)) {
      cellDestination = data.findFeasibleClassroom(cellIndex, child);
      if (cellDestination == -1) {
        cellDestination = data.findFeasibleDay(cellIndex, child);
        if (cellDestination == -1) {
          cellDestination = data.findFeasibleClassroomAndDay(cellIndex, child);
          if (cellDestination == -1) {
            // Harakiri
            return false;
          }
        }
      }
    }

    child.setVariableValue(cellDestination, father.getVariableValue(cellIndex));
    child.setVariableValue(cellDestination + 10, father.getVariableValue(cellIndex + 10));
    child.setVariableValue(father.getVariableValue(cellIndex + 10) + 10, cellDestination);

    return true;
  }
}