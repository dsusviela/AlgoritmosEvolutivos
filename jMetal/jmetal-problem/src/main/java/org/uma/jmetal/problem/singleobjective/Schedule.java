package org.uma.jmetal.problem.singleobjective;

import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.solution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.scheduledata.ScheduleDataHandler;

import java.util.*;

public class Schedule extends AbstractIntegerProblem {
  private int cellsInMatrix;
  private ScheduleDataHandler handler;

  public Schedule(ScheduleDataHandler dataHandler) {
    handler = dataHandler;
    handler.generateInstance();
    cellsInMatrix = handler.getClassroomCapacity().keySet().size() * 60;
    handler.setCellsInMatrix(cellsInMatrix);
    setNumberOfVariables(cellsInMatrix);
    setNumberOfObjectives(1);
    setName("Schedule");
    List<Integer> lowerLimit = new ArrayList<>(getNumberOfVariables());
    List<Integer> upperLimit = new ArrayList<>(getNumberOfVariables());

    for (int i = 0; i < getNumberOfVariables(); i++) {
      lowerLimit.add(-1);
      upperLimit.add(cellsInMatrix);
    }
    setLowerLimit(lowerLimit);
    setUpperLimit(upperLimit);
  }

  @Override
  public void evaluate(IntegerSolution solution) {
    LinkedList<Integer> solutionVector = new LinkedList<Integer>();
    // solution = createFeasibleSolution(solution);
    for (int i = 0; i < cellsInMatrix; i++) {
      solutionVector.add(solution.getVariableValue(i));
    }
    int fitness = 0;
    for (int c1 = 0; c1 < cellsInMatrix; c1++) {
      if (handler.isIndexClass(c1)) {
        for (int c2 = 0; c2 < cellsInMatrix; c2++) {
          if (handler.isIndexClass(c2)) {
            fitness += (c1 != c2 ? overlap(c1, c2, solution) : 0);
          }
        }
      }
    }
    fitness += classTurnDistributionDisparity(solution);
    fitness += distanceBetweenPair(solution);
    solution.setObjective(0, fitness);
  }

  private int distanceBetweenPair(IntegerSolution solution) {
    int consecutivePairs = 0;
    HashSet<Integer> evaluated = new HashSet<Integer>();
    for (int cellIndex = 0; cellIndex < cellsInMatrix; cellIndex++) {
      if (!evaluated.contains(cellIndex) && handler.isIndexClass(cellIndex) && handler.hasPair(cellIndex, solution)) {
        // we need to get the distance between the classes of the same pair
        int day = handler.getDay(cellIndex);
        int pairIndex = solution.getVariableValue(cellIndex + 10);
        int dayPair = handler.getDay(pairIndex);
        if (handler.distanceBetweenDays(day, dayPair) < 1) {
          consecutivePairs++;
        }
        evaluated.add(cellIndex);
        evaluated.add(pairIndex);
      }
    }
    return consecutivePairs * handler.getConsecutivePenaltyFactor();
  }

  private int overlap(int courseIndex1, int courseIndex2, IntegerSolution solution) {
    // checking if both classes actually collide
    if (!(handler.getTurn(courseIndex1) == handler.getTurn(courseIndex2)
        && handler.getDay(courseIndex1) == handler.getDay(courseIndex1))) {
      return 0;
    }
    // get the orientations affected by the collision
    int course1 = solution.getVariableValue(courseIndex1) / 10;
    int course2 = solution.getVariableValue(courseIndex2) / 10;
    HashSet<Integer> collidingOrientations = new HashSet<Integer>();
    for (Integer orientation : handler.getCourseMapOrientation().get(course1)) {
      if (handler.getCourseMapOrientation().get(course2).contains(orientation)
          && handler.getCourseMapYear().get(course1) == handler.getCourseMapYear().get(course2)) {
        collidingOrientations.add(orientation);
      }
    }
    // if there are no orientations, then there can be no students
    if (collidingOrientations.isEmpty()) {
      return 0;
    }

    int affectedStudents = 0;
    for (Integer orientation : collidingOrientations) {
      affectedStudents += handler.getOrientationStudents().get(orientation)
          * handler.getRateOfYearlyDecay().get(handler.getCourseMapYear().get(course1));
    }

    return affectedStudents;
  }

  private int classTurnDistributionDisparity(IntegerSolution matrix) {
    // initializing structures
    int disparity = 0;
    HashMap<Integer, HashMap<Integer, Integer>> courseHeatmap = new HashMap<Integer, HashMap<Integer, Integer>>();
    for (int course = 0; course < handler.getAmountCourses(); course++) {
      HashMap<Integer, Integer> turnHeatmap = new HashMap<Integer, Integer>();
      for (int turn = 0; turn < 3; turn++) {
        turnHeatmap.put(turn, 0);
      }
      courseHeatmap.put(course, turnHeatmap);
    }
    // populating heatmap
    for (int courseIndex = 0; courseIndex < cellsInMatrix; courseIndex++) {
      if (!handler.isIndexClass(courseIndex) || handler.isAvailable(courseIndex, matrix)) {
        continue;
      }
      int course = handler.getClassCourse(matrix.getVariableValue(courseIndex));
      int turn = handler.getTurn(courseIndex);
      HashMap<Integer, Integer> turnCourseMap = courseHeatmap.get(course);
      int coursesInTurn = turnCourseMap.get(turn);
      turnCourseMap.put(turn, coursesInTurn++);
      courseHeatmap.put(course, turnCourseMap);
    }
    // calculate disparity by course
    for (HashMap<Integer, Integer> map : courseHeatmap.values()) {
      Integer minForCourse = Collections.min(map.values());
      Integer maxForCourse = Collections.max(map.values());
      disparity += (maxForCourse - minForCourse) * handler.getDisparityFactor();
    }
    return disparity;
  }

  @Override
  public IntegerSolution createSolution() {
    // initialize empty solution
    IntegerSolution solution = new DefaultIntegerSolution(this);
    for (int index = 0; index < cellsInMatrix; index++) {
      solution.setVariableValue(index, -1);
    }
    // we get all courses
    HashMap<Integer, ArrayList<Integer>> courseMapClasses = handler.getCourseMapClasses();
    for (Integer course : courseMapClasses.keySet()) {
      ArrayList<Integer> classTypes = courseMapClasses.get(course);
      for (int type = 0; type < 4; type++) {
        for (int amountClassesType = 0; amountClassesType < classTypes.get(type); amountClassesType++) {
          int classWithType = course * 10 + type;
          HashMap<Integer, ArrayList<Integer>> classroomData = new HashMap<Integer, ArrayList<Integer>>();
          if (type < 2) {
            classroomData = handler.getFeasibleClassroomsWithPair(classWithType, solution);
          } else {
            classroomData = handler.getFeasibleClassroomsNoPair(classWithType, solution);
          }
          if (classroomData.isEmpty()) {
            // we are in deep trouble
            // we need to find a candidate to swap with us
            if (type < 2) {
              solution = handler.insertPairIntoSolution(classWithType, solution);
            } else {
              solution = handler.insertClassIntoSolution(classWithType, solution);
            }
            if (solution == null) {
              // we got to try again since this random went wrong and cant
              // be salvaged
              return createSolution();
            }
            continue;
          }
          ArrayList<Integer> option = classroomData
              .get(JMetalRandom.getInstance().nextInt(0, classroomData.size() - 1));
          int targetCell = 60 * option.get(0) + 20 * option.get(1) + 2 * option.get(2) + option.get(3);
          solution.setVariableValue(targetCell, classWithType);
          solution.setVariableValue(targetCell + 10, targetCell);
          // we must also set the pair
          if (type < 2) {
            int pairCell = 60 * option.get(0) + 20 * option.get(1) + 2 * option.get(4) + option.get(5);
            solution.setVariableValue(pairCell, classWithType);
            solution.setVariableValue(targetCell + 10, pairCell);
            solution.setVariableValue(pairCell + 10, targetCell);
          }
        }
      }
    }
    return solution;
  }
}
