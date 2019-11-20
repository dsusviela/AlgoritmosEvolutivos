package org.uma.jmetal.problem.singleobjective;

import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.solution.impl.DefaultIntegerSolution;

import java.util.*;

public class Schedule extends AbstractIntegerProblem {
  private int cellsInMatrix = 1110;
  private int amountCourses;
  private int disparityFactor;
  private HashMap<Integer, HashSet<Integer>> courseMap;
  private HashMap<Integer, Integer> classStudents;

  public Schedule() {
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
  }

  @Override
  public void evaluate(IntegerSolution solution) {
    LinkedList<Integer> solutionVector = new LinkedList<Integer>();
    for (int i = 0; i < cellsInMatrix; i++) {
      solutionVector.add(solution.getVariableValue(i));
    }
    int fitness = 0;
    for (int c1 = 0; c1 < cellsInMatrix; c1++) {
      if (isIndexClass(c1)) {
        for (int c2 = 0; c2 < cellsInMatrix; c2++) {
          if (isIndexClass(c2)) {
            fitness += (c1 != c2 ? overlap(c1, c2, solution) : 0);
          }
        }
      }
    }
    fitness += distributionDisparity(solution);
    solution.setObjective(0, fitness);
  }

  private int overlap(int c1, int c2, IntegerSolution matrix) {
    int affectedStudents = 0;
    // checking if both classes actually collide
    if (!(getTurn(c1) == getTurn(c2) &&
        getDay(c1) == getDay(c1))) {
      return affectedStudents;
    }
    // get the orientations affected by the collision
    Integer class1 = new Integer(matrix.getVariableValue(c1));
    Integer class2 = new Integer(matrix.getVariableValue(c2));
    HashSet<Integer> collidingOrientations = new HashSet<Integer>();
    for (Integer orientation : courseMap.get(class1)) {
      if (courseMap.get(class2).contains(orientation)) {
        collidingOrientations.add(orientation);
      }
    }
    // if there are no orientations, then there can be no students
    if (collidingOrientations.isEmpty()) {
      return affectedStudents;
    }
    // check if there is a third courses that collides aswell
    boolean jumpDay = true;
    int startingCell = getDay(c1)*2 + getTurn(c1)*20;
    for (int c3 = startingCell; c3 < cellsInMatrix; c3 += (jumpDay ? 59 : 1)) {
      Integer class3 = new Integer(matrix.getVariableValue(c3));
      if (c3 != c1 && c3 != c2) {
        for (Integer collidingOrientation : collidingOrientations) {
          if (courseMap.get(class3).contains(collidingOrientation)) {
            affectedStudents += classStudents.get(class3);
          }
        }
      }
      jumpDay = !jumpDay;
    }
    return affectedStudents;
  }

  private int getDay(int index) {
    return (index % 10)/2;
  }

  private int getTurn(int index) {
    return (index / 20) % 3;
  }

  private boolean isIndexClass(int index) {
    return (index % 20) < 10;
  }

  private int distributionDisparity(IntegerSolution matrix) {
    // initializing structures
    int disparity = 0;
    HashMap<Integer, HashMap<Integer, Integer>> courseHeatmap =
        new HashMap<Integer, HashMap<Integer, Integer>>();
    for (int course = 0; course < amountCourses; course++) {
      HashMap<Integer, Integer> turnHeatmap = new HashMap<Integer, Integer>();
      for (int turn = 0; turn < 3; turn++) {
        turnHeatmap.put(turn, new Integer(0));
      }
      courseHeatmap.put(new Integer(course), turnHeatmap);
    }
    // populating heatmap
    for (int courseIndex = 0; courseIndex < cellsInMatrix; courseIndex++) {
      if (!isIndexClass(courseIndex)) {
        continue;
      }
      Integer course = new Integer(matrix.getVariableValue(courseIndex));
      Integer turn = new Integer(getTurn(courseIndex));
      HashMap<Integer, Integer> turnCourseMap = courseHeatmap.get(course);
      Integer coursesInTurn = turnCourseMap.get(turn);
      turnCourseMap.put(turn, coursesInTurn++);
      courseHeatmap.put(course, turnCourseMap);
    }
    // calculate disparity by course
    for (HashMap<Integer, Integer> map : courseHeatmap.values()) {
      Integer minForCourse = Collections.min(map.values());
      Integer maxForCourse = Collections.max(map.values());
      disparity += (maxForCourse - minForCourse)*disparityFactor;
    }
    return disparity;
  }

  @Override
  public IntegerSolution createSolution() {
    return new DefaultIntegerSolution(this);
  }
}
