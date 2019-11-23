package org.uma.jmetal.problem.singleobjective;

import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.solution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.scheduledata.ScheduleDataHandler;

import java.util.*;

public class Schedule extends AbstractIntegerProblem {
  private int cellsInMatrix = 1110;
  private ScheduleDataHandler handler;

  public Schedule(ScheduleDataHandler dataHandler) {
    handler = dataHandler;
    setNumberOfVariables(cellsInMatrix);
    setNumberOfObjectives(1);
    setName("Schedule");
    List<Integer> lowerLimit = new ArrayList<>(getNumberOfVariables()) ;
    List<Integer> upperLimit = new ArrayList<>(getNumberOfVariables()) ;

    for (int i = 0; i < getNumberOfVariables(); i++) {
      lowerLimit.add(0);
      upperLimit.add(cellsInMatrix);
    }
    setLowerLimit(lowerLimit);
    setUpperLimit(upperLimit);

    handler.generateInstance();
  }

  @Override
  public void evaluate(IntegerSolution solution) {
    LinkedList<Integer> solutionVector = new LinkedList<Integer>();
    solution = createFeasibleSolution(solution);
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
    solution.setObjective(0, fitness);
  }
  
  private IntegerSolution createFeasibleSolution(IntegerSolution solution) {
    solution = checkClassroomCapacity(solution);
    solution = checkPairsDay(solution);
    solution = checkTurnDay(solution);
    return solution;
  }

  private IntegerSolution checkClassroomCapacity(IntegerSolution solution) {
    for (int cellIndex = 0; cellIndex < cellsInMatrix; cellIndex++) {
      // we only want to iterate in the cells that have classes
      if (handler.isIndexClass(cellIndex) && handler.indexHasClass(cellIndex, solution)) {
        int classroom = handler.getClassroom(cellIndex);
        int capacity = handler.getClassroomCapacity(classroom);
        int attendingStudents = handler.getAttendingStudents(solution.getVariableValue(cellIndex));
        // we now check if theres a capacity conflict
        if (capacity < attendingStudents) {
          // conflict resolution
          int turn = handler.getTurn(cellIndex);
          int day = handler.getDay(cellIndex);
          solution = handler.findFeasibleClassroom(attendingStudents, cellIndex, solution);
        }
      }
    }
    return solution;
  }

  private int overlap(int courseIndex1, int courseIndex2, IntegerSolution matrix) {
    int affectedStudents = 0;
    // checking if both classes actually collide
    if (!(handler.getTurn(courseIndex1) == handler.getTurn(courseIndex2) &&
        handler.getDay(courseIndex1) == handler.getDay(courseIndex1))) {
      return affectedStudents;
    }
    // get the orientations affected by the collision
    Integer course1 = new Integer(matrix.getVariableValue(courseIndex1) / 10);
    Integer course2 = new Integer(matrix.getVariableValue(courseIndex2) / 10);
    HashSet<Integer> collidingOrientations = new HashSet<Integer>();
    for (Integer orientation : handler.getCourseMapOrientation().get(course1)) {
      if (handler.getCourseMapOrientation().get(course2).contains(orientation)) {
        collidingOrientations.add(orientation);
      }
    }
    // if there are no orientations, then there can be no students
    if (collidingOrientations.isEmpty()) {
      return affectedStudents;
    }
    // check if there is a third courses that collides aswell
    boolean jumpDay = true;
    int startingCell = handler.getDay(courseIndex1)*2 + handler.getTurn(courseIndex1)*20;
    for (int courseIndex3 = startingCell; courseIndex3 < cellsInMatrix; courseIndex3 += (jumpDay ? 59 : 1)) {
      Integer class3 = new Integer(matrix.getVariableValue(courseIndex3) / 10);
      if (courseIndex3 != courseIndex1 && courseIndex3 != courseIndex2) {
        for (Integer collidingOrientation : collidingOrientations) {
          if (class3 == course2 && course2 == course1) {
            // if its the same course we must penalize even harder
            affectedStudents += handler.getClassStudents().get(class3) * 2;
          } else if (handler.getCourseMapOrientation().get(class3).contains(collidingOrientation)) {
            affectedStudents += handler.getClassStudents().get(class3);
          }
        }
      }
      jumpDay = !jumpDay;
    }
    return affectedStudents;
  }

  private int classTurnDistributionDisparity(IntegerSolution matrix) {
    // initializing structures
    int disparity = 0;
    HashMap<Integer, HashMap<Integer, Integer>> courseHeatmap =
        new HashMap<Integer, HashMap<Integer, Integer>>();
    for (int course = 0; course < handler.getAmountCourses(); course++) {
      HashMap<Integer, Integer> turnHeatmap = new HashMap<Integer, Integer>();
      for (int turn = 0; turn < 3; turn++) {
        turnHeatmap.put(turn, 0);
      }
      courseHeatmap.put(course, turnHeatmap);
    }
    // populating heatmap
    for (int courseIndex = 0; courseIndex < cellsInMatrix; courseIndex++) {
      if (!handler.isIndexClass(courseIndex)) {
        continue;
      }
      Integer course = new Integer(matrix.getVariableValue(courseIndex) / 10);
      Integer turn = handler.getTurn(courseIndex);
      HashMap<Integer, Integer> turnCourseMap = courseHeatmap.get(course);
      Integer coursesInTurn = turnCourseMap.get(turn);
      turnCourseMap.put(turn, coursesInTurn++);
      courseHeatmap.put(course, turnCourseMap);
    }
    // calculate disparity by course
    for (HashMap<Integer, Integer> map : courseHeatmap.values()) {
      Integer minForCourse = Collections.min(map.values());
      Integer maxForCourse = Collections.max(map.values());
      disparity += (maxForCourse - minForCourse)*handler.getDisparityFactor();
    }
    return disparity;
  }

  @Override
  public IntegerSolution createSolution() {
    return new DefaultIntegerSolution(this);
  }
}
