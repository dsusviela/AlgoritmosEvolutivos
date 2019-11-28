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

    System.out.println("EMPIEZO CRUZAMIENTO");

    return doCrossover(integerSolutions.get(0), integerSolutions.get(1));
  }

  private List<IntegerSolution> doCrossover(IntegerSolution parent1, IntegerSolution parent2) {
    if (crossoverRandomGenerator.getRandomValue() <= crossoverProbability) {
      System.out.println("CRUZANDO...");

      int crossoverType = JMetalRandom.getInstance().nextInt(0, 2);

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
          setUpChildCell(child, mother, father, classBalance, cellIndex);
        }
      }
    }

    // first three steps are done for the child
    // we now neeed to delete the excess
    deleteExcessFromChild(child, mother, classBalance);

    // we need to insert now
    for (int classroom = 0; classroom < data.getClassroomCapacity().keySet().size(); classroom++) {
      for (int turn = 0; turn < 3; turn++) {
        for (int cell = 0; cell < 2; cell++) {
          int cellIndex = 60 * classroom + 20 * turn + 2 * day + cell;
          child.setVariableValue(cellIndex, father.getVariableValue(cellIndex));
          child.setVariableValue(cellIndex + 10, father.getVariableValue(cellIndex + 10));
          if (data.hasPair(cellIndex, father)) {
            if (!solveCollision(father.getVariableValue(cellIndex + 10), child, father)) {
              System.out.println("CROSSOVER ABORTED");
              return (IntegerSolution) mother.copy();
            }
          }
        }
      }
    }

    addMissingClassesToChild(child, mother, classBalance);

    return child;
  }

  private List<IntegerSolution> turnCrossover(IntegerSolution parent1, IntegerSolution parent2) {
    Random random = new Random();
    int turn = random.nextInt(3);
    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
    offspring.add(doTurnCrossover(parent1, parent2, turn));
    offspring.add(doTurnCrossover(parent2, parent1, turn));
    return offspring;
  }

  private IntegerSolution doTurnCrossover(IntegerSolution father, IntegerSolution mother, int turn) {
    IntegerSolution child = (IntegerSolution) mother.copy();

    // register classes to delete from mother
    HashMap<Integer, Integer> classBalance = new HashMap<Integer, Integer>();
    for (int day = 0; day < 5; day++) {
      for (int classroom = 0; classroom < data.getClassroomCapacity().keySet().size(); classroom++) {
        for (int cell = 0; cell < 2; cell++) {
          int cellIndex = classroom * 60 + turn * 20 + day * 2 + cell;
          setUpChildCell(child, mother, father, classBalance, cellIndex);
        }
      }
    }

    // first three steps are done for the child
    // we now neeed to delete the excess
    deleteExcessFromChild(child, mother, classBalance);

    // we need to insert now
    for (int classroom = 0; classroom < data.getClassroomCapacity().keySet().size(); classroom++) {
      for (int day = 0; day < 5; day++) {
        for (int cell = 0; cell < 2; cell++) {
          int cellIndex = 60 * classroom + 20 * turn + 2 * day + cell;
          child.setVariableValue(cellIndex, father.getVariableValue(cellIndex));
          child.setVariableValue(cellIndex + 10, father.getVariableValue(cellIndex + 10));
          if (data.hasPair(cellIndex, father)) {
            if (!solveCollision(father.getVariableValue(cellIndex + 10), child, father)) {
              System.out.println("CROSSOVER ABORTED");
              return (IntegerSolution) mother.copy();
            }
          }
        }
      }
    }

    addMissingClassesToChild(child, mother, classBalance);

    return child;
  }

  private List<IntegerSolution> classroomCrossover(IntegerSolution parent1, IntegerSolution parent2) {
    Random random = new Random();
    int classroom = random.nextInt(data.getClassroomQty());
    List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);
    offspring.add(doClassroomCrossover(parent1, parent2, classroom));
    offspring.add(doClassroomCrossover(parent2, parent1, classroom));
    return offspring;
  }

  private IntegerSolution doClassroomCrossover(IntegerSolution father, IntegerSolution mother, int classroom) {
    IntegerSolution child = (IntegerSolution) mother.copy();

    // register classes to delete from mother
    HashMap<Integer, Integer> classBalance = new HashMap<Integer, Integer>();
    for (int day = 0; day < 5; day++) {
      for (int turn = 0; turn < 3; turn++) {
        for (int cell = 0; cell < 2; cell++) {
          int cellIndex = classroom * 60 + turn * 20 + day * 2 + cell;
          setUpChildCell(child, mother, father, classBalance, cellIndex);
        }
      }
    }

    // first three steps are done for the child
    // we now neeed to delete the excess
    deleteExcessFromChild(child, mother, classBalance);

    // we need to insert now
    for (int turn = 0; turn < 3; turn++) {
      for (int day = 0; day < 5; day++) {
        for (int cell = 0; cell < 2; cell++) {
          int cellIndex = 60 * classroom + 20 * turn + 2 * day + cell;
          child.setVariableValue(cellIndex, father.getVariableValue(cellIndex));
          child.setVariableValue(cellIndex + 10, father.getVariableValue(cellIndex + 10));
          if (data.hasPair(cellIndex, father)) {
            if (!solveCollision(father.getVariableValue(cellIndex + 10), child, father)) {
              System.out.println("CROSSOVER ABORTED");
              return (IntegerSolution) mother.copy();
            }
          }
        }
      }
    }

    addMissingClassesToChild(child, mother, classBalance);

    return child;
  }

  private void setUpChildCell(IntegerSolution child, IntegerSolution mother, IntegerSolution father,
      HashMap<Integer, Integer> classBalance, int cellIndex) {
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

  private void deleteExcessFromChild(IntegerSolution child, IntegerSolution mother,
      HashMap<Integer, Integer> classBalance) {
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
  }

  private void addMissingClassesToChild(IntegerSolution child, IntegerSolution mother,
      HashMap<Integer, Integer> classBalance) {
    // if the balance is negative, add the missing classes
    for (int typeOfClass = 0; typeOfClass < 4; typeOfClass++) {
      for (int course = 0; course < data.getAmountCourses() * 4; course++) {
        int classWithType = course * 10 + typeOfClass;
        if (classBalance.containsKey(classWithType)) {
          while (classBalance.get(classWithType) < 0) {
            if (data.getClassType(classWithType) < 2) {
              child = data.insertPairIntoSolution(classWithType, child);
              if (child == null) {
                System.out.println("CROSSOVER ABORTED");
                child = (IntegerSolution) mother.copy();
                return;
              } else {
                classBalance.put(classWithType, classBalance.get(classWithType) - 1);
              }
            } else {
              child = data.insertClassIntoSolution(classWithType, child);
              if (child == null) {
                System.out.println("CROSSOVER ABORTED");
                child = (IntegerSolution) mother.copy();
                return;
              } else {
                classBalance.put(classWithType, classBalance.get(classWithType) - 1);
              }
            }
          }
        }
      }
    }
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