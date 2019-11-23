package org.uma.jmetal.util.scheduledata;

import org.uma.jmetal.solution.IntegerSolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ScheduleDataHandler {
  // total cells in matrix
  private int cellsInMatrix = 1110;
  // total courses in matrix
  private int amountCourses;
  // how 'wrong' is to have uneven distribution of courses over the turns in a given day
  private int disparityFactor;
  // given a course returns its orientations
  private HashMap<Integer, HashSet<Integer>> courseMapOrientation;
  // given a course returns its classes in order
  // position 0 refers to lectures, 1 to practice, 2 single practice, 3 to single lecture
  private HashMap<Integer, ArrayList<Integer>> courseMapClasses;
  // given the string id of a classroom returns the capacity
  private HashMap<String, Integer> classroomCapacity;
  // given the number id of a classroom returns the string id
  private HashMap<Integer, String> classroomNameMap;
  // given a class returns the number of students
  private HashMap<Integer, Integer> classStudents;
  // given an orientation returns the number of students
  private HashMap<Integer, Integer> orientationStudents;
  // given a year returns the factor of student decay for said year
  // please note that course 44 in deault data has a special factor
  private HashMap<Integer, Float> attendanceFactor;
  // given a course returns the year it corresponds to.
  // please note that course 44 in deault data has a special year
  private HashMap<Integer, Integer> courseMapYear;

  ScheduleDataHandler() {
    generateInstance();
  }

  /*
  * GETTERS AND SETTERS OF ALL FIELDS
  */

  public int getCellsInMatrix() {
    return cellsInMatrix;
  }

  public void setCellsInMatrix(int cellsInMatrix) {
    this.cellsInMatrix = cellsInMatrix;
  }

  public int getAmountCourses() {
    return amountCourses;
  }

  public void setAmountCourses(int amountCourses) {
    this.amountCourses = amountCourses;
  }

  public int getDisparityFactor() {
    return disparityFactor;
  }

  public void setDisparityFactor(int disparityFactor) {
    this.disparityFactor = disparityFactor;
  }

  public HashMap<Integer, HashSet<Integer>> getCourseMapOrientation() {
    return courseMapOrientation;
  }

  public void setCourseMapOrientation(HashMap<Integer, HashSet<Integer>> courseMapOrientation) {
    this.courseMapOrientation = courseMapOrientation;
  }

  public HashMap<Integer, ArrayList<Integer>> getCourseMapClasses() {
    return courseMapClasses;
  }

  public void setCourseMapClasses(HashMap<Integer, ArrayList<Integer>> courseMapClasses) {
    this.courseMapClasses = courseMapClasses;
  }

  public HashMap<String, Integer> getClassroomCapacity() {
    return classroomCapacity;
  }

  public Integer getClassroomCapacity(int classroom) {
    String clas = classroomNameMap.get(classroom);
    return classroomCapacity.get(clas);
  }

  public void setClassroomCapacity(HashMap<String, Integer> classroomCapacity) {
    this.classroomCapacity = classroomCapacity;
  }

  public HashMap<Integer, String> getClassroomNameMap() {
    return classroomNameMap;
  }

  public void setClassroomNameMap(HashMap<Integer, String> classroomNameMap) {
    this.classroomNameMap = classroomNameMap;
  }

  public HashMap<Integer, Integer> getClassStudents() {
    return classStudents;
  }

  public void setClassStudents(HashMap<Integer, Integer> classStudents) {
    this.classStudents = classStudents;
  }

  public HashMap<Integer, Integer> getOrientationStudents() {
    return orientationStudents;
  }

  public void setOrientationStudents(HashMap<Integer, Integer> orientationStudents) {
    this.orientationStudents = orientationStudents;
  }

  public HashMap<Integer, Float> getAttendanceFactor() {
    return attendanceFactor;
  }

  public void setAttendanceFactor(HashMap<Integer, Float> attendanceFactor) {
    this.attendanceFactor = attendanceFactor;
  }

  public HashMap<Integer, Integer> getCourseMapYear() {
    return courseMapYear;
  }

  public void setCourseMapYear(HashMap<Integer, Integer> courseMapYear) {
    this.courseMapYear = courseMapYear;
  }

  /*
  * PUBLIC HELPER FUNCTIONS
  */

  // given the *value* of a cell returns the course
  public int getClassCourse(Integer classWithType) {
    return classWithType / 10;
  }
  // given the *value* of a cell returns the type
  public int getClassType(Integer classWithType) {
    return classWithType % 10;
  }
  // returns the fraction of classes of the given type for the given course
  public Float getTypeProportionInCourse(int type, int course) {
    return (1f / courseMapClasses.get(course).get(type));
  }
  // given an index of the solution returns the corresponding classroom
  public int getClassroom(int cellIndex) {
    return (cellIndex / 60);
  }
  // given a cellIndex and a solution returns if the value in cellIndex has a pair
  public boolean hasPair(int cellIndex, IntegerSolution solution) {
    return ((solution.getVariableValue(cellIndex) + 10) != 0 &&
        solution.getVariableValue(cellIndex)+10 == solution.getVariableValue(cellIndex));
  }
  // returns if the index has a class
  public boolean indexHasClass(int cellIndex, IntegerSolution solution) {
    return (solution.getVariableValue(cellIndex) != 0);
  }
  // given an index of the matrix returns the day
  public int getDay(int index) {
    return (index % 10)/2;
  }
  // given an index of the matrix returns the Turn
  public int getTurn(int index) {
    return (index / 20) % 3;
  }
  // returns whether the given index is an index class or a pair class
  public boolean isIndexClass(int index) {
    return (index % 20) < 10;
  }
  // given 2 days returns the amount of days between them
  public int distanceBetweenDays(int day, int dayPair) {
    return Math.abs((day - dayPair));
  }

  public IntegerSolution findFeasibleClassroom(int cellIndex,
                                               IntegerSolution solution) {
    int attendingStudents = getAttendingStudents(solution.getVariableValue(cellIndex));
    return findFeasibleClassroom(attendingStudents, cellIndex, solution);
  }

  public IntegerSolution findFeasibleClassroom(int attendingStudents,
                                               int cellIndex,
                                               IntegerSolution originalSolution) {
    IntegerSolution solution = (IntegerSolution) originalSolution.copy(); // debug: cast may not be the way
    int turn = getTurn(cellIndex);
    int day = getDay(cellIndex);
    int startingCell = day*2 + turn*20;
    boolean skipDay = true;
    for (int cellCandidate = startingCell; cellCandidate < cellsInMatrix;
         cellCandidate += (skipDay ? 59 : 1)) {
      // check if candidate is available
      if (solution.getVariableValue(cellCandidate) == 0) {
        // get the capacity of the candidate
        int classroom = getClassroom(cellIndex);
        int capacity = getClassroomCapacity(classroom);
        if (attendingStudents <= capacity) {
          // candidate is fit for insertion
          int classOrigin = solution.getVariableValue(cellIndex);
          int classPair = solution.getVariableValue(cellIndex + 10);
          int cellCandidatePair = cellCandidate + 10;
          // make the insertion
          solution.setVariableValue(cellCandidate, classOrigin);
          if (hasPair(cellIndex, originalSolution)) {
            solution.setVariableValue(cellCandidatePair, classPair);
          } else {
            solution.setVariableValue(cellCandidatePair, cellCandidate);
          }
          solution.setVariableValue(cellIndex, 0);
          solution.setVariableValue(cellIndex + 10, 0);
          // pair should now point to the new value
          if (hasPair(cellIndex, originalSolution)) {
            solution.setVariableValue(classPair + 10, cellCandidate);
          }
          return solution;
        }
      }
      skipDay = !skipDay;
    }
    // no empty classes were found, looking to swap
    return swapFeasibleClassroom(attendingStudents, cellIndex, solution);
  }

  private IntegerSolution swapFeasibleClassroom(int attendingStudents,
                                                int cellIndex,
                                                IntegerSolution originalSolution) {
    IntegerSolution solution = (IntegerSolution) originalSolution.copy();
    int turn = getTurn(cellIndex);
    int day = getDay(cellIndex);
    int startingCell = day*2 + turn*20;
    HashSet<Integer> victimSet = new HashSet<Integer>();
    boolean skipDay = true;
    for (int cellCandidate = startingCell; cellCandidate < cellsInMatrix;
         cellCandidate += (skipDay ? 59 : 1)) {
      // check if its not the same cell or if its empty
      // note that all empty indexes were discarded in parent function
      if (cellIndex == cellCandidate ||
          solution.getVariableValue(cellCandidate) == 0) {
        // we should continue
        skipDay = !skipDay;
        continue;
      }
      // add the class to the victim list
      int candidateClassroom = getClassroom(cellCandidate);
      int candidateCapacity = getClassroomCapacity(candidateClassroom);
      if (attendingStudents < candidateCapacity) {
        victimSet.add(cellCandidate);
      }
    }
    // note that the possibleVictim set cant be empty, if it is then theres no classroom that
    // can accommodate attending students
    int victim = -1;
    int victimAttendance = attendingStudents;
    for (Integer possibleVictim : victimSet) {
      int victimClassWithType = solution.getVariableValue(possibleVictim);
      int attendance = getAttendingStudents(victimClassWithType);
      if (attendance < victimAttendance) {
        victim = possibleVictim;
        victimAttendance = attendance;
      }
    }
    // we now have a victim, we can swap the victim with the origin
    // first we check if the swap is possible
    int originCapacity = getClassroomCapacity(getClassroom(cellIndex));
    if (victimAttendance <= originCapacity) {
      // initialize variables
      int victimCopy = solution.getVariableValue(victim);
      int vicitmPairCopy = solution.getVariableValue(victim + 10);
      int originalValue = solution.getVariableValue(cellIndex);
      int originalPairValue = solution.getVariableValue(cellIndex + 10);
      // perform the swap
      solution.setVariableValue(victim, originalValue);
      solution.setVariableValue(cellIndex, victimCopy);
      if (hasPair(victim, originalSolution)) {
        solution.setVariableValue(cellIndex + 10, vicitmPairCopy);
        // pair now should reference new value
        solution.setVariableValue(vicitmPairCopy + 10, cellIndex);
      }
      if (hasPair(cellIndex, originalSolution)) {
        solution.setVariableValue(victim + 10, originalPairValue);
        // pair now should reference new value
        solution.setVariableValue(originalPairValue + 10, victim);
      }
      return solution;
    } else {
      // no eligible victims were found
      return null;
    }
  }

  public int getAttendingStudents(Integer classWithType) {
    int type = getClassType(classWithType);
    int course = getClassCourse(classWithType);
    int amountOfStudents = 0;
    for (Integer orientation : getCourseMapOrientation().get(course)) {
      amountOfStudents += getOrientationStudents().get(orientation);
    }
    int year = getCourseMapYear().get(course);
    amountOfStudents = (int) (getAttendanceFactor().get(year) *
        amountOfStudents * getTypeProportionInCourse(type, course));
    return amountOfStudents;
  }

  public IntegerSolution findFeasibleDay(int cellIndex, IntegerSolution originalSolution) {
    IntegerSolution solution = (IntegerSolution) originalSolution.copy();
    int classWithType = solution.getVariableValue(cellIndex);
    // note that the classroom already fits the amount of students
    // we need to find another day for this class in the same room at the same turn
    int turn = getTurn(cellIndex);
    int classroom = getClassroom(cellIndex);
    HashSet<Integer> candidateDays = getCandidateDaysForPair(cellIndex, solution);
    // we iterate through all the classroom slots for the same turn as the original
    for (Integer day : candidateDays) {
      int candidateCell = 60*classroom + 20*turn + 2*day;
      // if its not the same as the original and its available
      if (candidateCell != classroom &&
          solution.getVariableValue(candidateCell) == 0) {
        // we can perform the swap
        int originalValue = solution.getVariableValue(cellIndex);
        int originalPair = solution.getVariableValue(cellIndex + 10);
        solution.setVariableValue(candidateCell, originalValue);
        solution.setVariableValue(candidateCell + 10, originalPair);
        // now we must make the pair point at the new cell
        // note that the pair always exists, else we wouldnt need for the function call
        solution.setVariableValue(originalPair + 10, candidateCell);
        return solution;
      }
    }
    // we didnt find a slot, we must perform a swap
    
    return solution;
  }

  private HashSet<Integer> getCandidateDaysForPair(int cellIndex, IntegerSolution solution) {
    HashSet<Integer> result = new HashSet<Integer>();
    int cellPair = solution.getVariableValue(cellIndex + 10);
    int day = getDay(cellIndex);
    for (int candidateDay = 0; candidateDay < 5; candidateDay++) {
      if (1 < distanceBetweenDays(candidateDay, day)) {
        result.add(candidateDay);
      }
    }
    return result;
  }

  // default instance
  // creates an instance problem
  public void generateInstance() {
    amountCourses = 63;
    classroomCapacity = new HashMap<String, Integer>();
    classroomCapacity.put("31", 50);
    classroomCapacity.put("101", 55);
    classroomCapacity.put("102", 55);
    classroomCapacity.put("115", 42);
    classroomCapacity.put("116", 25);
    classroomCapacity.put("301", 130);
    classroomCapacity.put("303", 110);
    classroomCapacity.put("305", 60);
    classroomCapacity.put("307", 350);
    classroomCapacity.put("309", 32);
    classroomCapacity.put("310", 32);
    classroomCapacity.put("311", 60);
    classroomCapacity.put("501", 130);
    classroomCapacity.put("601", 110);
    classroomCapacity.put("ACTOS", 300);
    classroomCapacity.put("502", 50);
    classroomCapacity.put("703", 45);
    classroomCapacity.put("705", 40);
    classroomCapacity.put("720", 40);
    classroomCapacity.put("722", 50);
    classroomCapacity.put("725", 40);
    classroomCapacity.put("727", 50);
    classroomCapacity.put("A01", 300);
    classroomCapacity.put("A11", 100);
    classroomCapacity.put("A12", 220);
    classroomCapacity.put("A21", 140);
    classroomCapacity.put("A22", 220);
    classroomCapacity.put("B01", 380);
    classroomCapacity.put("B11", 90);
    classroomCapacity.put("B12", 250);
    classroomCapacity.put("B21", 70);
    classroomCapacity.put("B22", 70);
    classroomCapacity.put("C01", 200);
    classroomCapacity.put("C11", 100);
    classroomCapacity.put("C12", 220);
    classroomCapacity.put("C21", 100);
    classroomCapacity.put("C22", 220);
    classroomCapacity.put("UDELAR A", 21);
    classroomCapacity.put("UDELAR B", 21);
    classroomCapacity.put("UDELAR C", 21);
    classroomCapacity.put("UDELAR D", 21);
    classroomCapacity.put("BIBLIOTECA", 10);
    classroomCapacity.put("312", 42);
    classroomCapacity.put("314", 44);
    classroomCapacity.put("315", 30);
    classroomCapacity.put("401", 45);
    classroomCapacity.put("402", 36);
    classroomCapacity.put("SW", 16);

    classroomNameMap = new HashMap<Integer, String>();
    int key = 0;
    for (String stringNameId : classroomCapacity.keySet()) {
      classroomNameMap.put(key, stringNameId);
      key++;
    }

    courseMapOrientation = new HashMap<Integer, HashSet<Integer>>();
    HashSet<Integer> orientationSet = new HashSet<>();
    orientationSet = new HashSet<>();
    orientationSet.add(0);
    orientationSet.add(1);
    orientationSet.add(2);
    orientationSet.add(3);
    orientationSet.add(4);
    orientationSet.add(5);
    orientationSet.add(6);
    orientationSet.add(9);
    orientationSet.add(10);
    orientationSet.add(11);
    courseMapOrientation.put(0, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(0);
    orientationSet.add(1);
    orientationSet.add(2);
    orientationSet.add(3);
    orientationSet.add(4);
    orientationSet.add(5);
    orientationSet.add(6);
    orientationSet.add(9);
    orientationSet.add(10);
    orientationSet.add(11);
    courseMapOrientation.put(1, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(0);
    orientationSet.add(1);
    orientationSet.add(2);
    orientationSet.add(3);
    orientationSet.add(4);
    orientationSet.add(5);
    orientationSet.add(6);
    orientationSet.add(9);
    orientationSet.add(10);
    orientationSet.add(11);
    orientationSet.add(12);
    courseMapOrientation.put(2, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(0);
    orientationSet.add(1);
    orientationSet.add(2);
    orientationSet.add(5);
    orientationSet.add(6);
    orientationSet.add(10);
    courseMapOrientation.put(3, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(3);
    orientationSet.add(9);
    courseMapOrientation.put(4, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(0);
    orientationSet.add(1);
    orientationSet.add(3);
    orientationSet.add(5);
    orientationSet.add(6);
    orientationSet.add(9);
    orientationSet.add(10);
    orientationSet.add(11);
    courseMapOrientation.put(5, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(0);
    orientationSet.add(1);
    orientationSet.add(2);
    orientationSet.add(3);
    orientationSet.add(4);
    orientationSet.add(5);
    orientationSet.add(6);
    orientationSet.add(9);
    orientationSet.add(10);
    courseMapOrientation.put(6, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(0);
    orientationSet.add(1);
    orientationSet.add(3);
    orientationSet.add(5);
    orientationSet.add(6);
    orientationSet.add(9);
    orientationSet.add(10);
    orientationSet.add(11);
    courseMapOrientation.put(7, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(0);
    courseMapOrientation.put(8, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(0);
    orientationSet.add(1);
    orientationSet.add(2);
    orientationSet.add(4);
    courseMapOrientation.put(9, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(1);
    orientationSet.add(2);
    orientationSet.add(3);
    orientationSet.add(5);
    orientationSet.add(6);
    orientationSet.add(10);
    courseMapOrientation.put(10, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(1);
    orientationSet.add(2);
    orientationSet.add(3);
    orientationSet.add(5);
    orientationSet.add(6);
    orientationSet.add(11);
    courseMapOrientation.put(11, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(1);
    orientationSet.add(2);
    orientationSet.add(11);
    courseMapOrientation.put(12, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(4);
    courseMapOrientation.put(13, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(4);
    courseMapOrientation.put(14, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(4);
    orientationSet.add(9);
    courseMapOrientation.put(15, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(2);
    orientationSet.add(5);
    orientationSet.add(6);
    orientationSet.add(12);
    courseMapOrientation.put(16, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(0);
    courseMapOrientation.put(17, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(0);
    courseMapOrientation.put(18, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(0);
    courseMapOrientation.put(19, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(0);
    orientationSet.add(1);
    courseMapOrientation.put(20, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(1);
    courseMapOrientation.put(21, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(1);
    courseMapOrientation.put(22, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(1);
    courseMapOrientation.put(23, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(1);
    orientationSet.add(2);
    orientationSet.add(4);
    courseMapOrientation.put(24, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(2);
    courseMapOrientation.put(25, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(2);
    orientationSet.add(4);
    courseMapOrientation.put(26, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(2);
    orientationSet.add(5);
    orientationSet.add(6);
    orientationSet.add(12);
    courseMapOrientation.put(27, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(3);
    orientationSet.add(9);
    courseMapOrientation.put(28, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(3);
    orientationSet.add(9);
    courseMapOrientation.put(29, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(3);
    courseMapOrientation.put(30, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(3);
    courseMapOrientation.put(31, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(3);
    orientationSet.add(9);
    courseMapOrientation.put(32, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(4);
    courseMapOrientation.put(33, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(4);
    courseMapOrientation.put(34, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(4);
    courseMapOrientation.put(35, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(5);
    orientationSet.add(6);
    courseMapOrientation.put(36, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(6);
    courseMapOrientation.put(37, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(6);
    courseMapOrientation.put(38, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(7);
    orientationSet.add(8);
    orientationSet.add(11);
    courseMapOrientation.put(39, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(7);
    orientationSet.add(8);
    orientationSet.add(11);
    courseMapOrientation.put(40, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(9);
    courseMapOrientation.put(41, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(9);
    courseMapOrientation.put(42, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(10);
    courseMapOrientation.put(43, orientationSet);

    orientationSet = new HashSet<>();
    orientationSet.add(0);
    orientationSet.add(1);
    orientationSet.add(3);
    orientationSet.add(5);
    orientationSet.add(6);
    orientationSet.add(10);
    orientationSet.add(12);
    courseMapOrientation.put(44, orientationSet);

    courseMapClasses = new HashMap<Integer, ArrayList<Integer>>();
    for (int i = 0; i < 45; i++) {
      courseMapClasses.put(i, new ArrayList<Integer>());
    }

    courseMapClasses.get(0).add(0, 5);
    courseMapClasses.get(0).add(1, 12);
    courseMapClasses.get(0).add(2, 0);
    courseMapClasses.get(0).add(3, 0);

    courseMapClasses.get(1).add(0, 5);
    courseMapClasses.get(1).add(1, 15);
    courseMapClasses.get(1).add(2, 0);
    courseMapClasses.get(1).add(3, 0);

    courseMapClasses.get(2).add(0, 5);
    courseMapClasses.get(2).add(1, 15);
    courseMapClasses.get(2).add(2, 5);
    courseMapClasses.get(2).add(3, 0);

    courseMapClasses.get(3).add(0, 0);
    courseMapClasses.get(3).add(1, 0);
    courseMapClasses.get(3).add(2, 7);
    courseMapClasses.get(3).add(3, 2);

    courseMapClasses.get(4).add(0, 0);
    courseMapClasses.get(4).add(1, 0);
    courseMapClasses.get(4).add(2, 5);
    courseMapClasses.get(4).add(3, 0);

    courseMapClasses.get(5).add(0, 3);
    courseMapClasses.get(5).add(1, 6);
    courseMapClasses.get(5).add(2, 0);
    courseMapClasses.get(5).add(3, 0);

    courseMapClasses.get(6).add(0, 4);
    courseMapClasses.get(6).add(1, 10);
    courseMapClasses.get(6).add(2, 0);
    courseMapClasses.get(6).add(3, 0);

    courseMapClasses.get(7).add(0, 1);
    courseMapClasses.get(7).add(1, 12);
    courseMapClasses.get(7).add(2, 0);
    courseMapClasses.get(7).add(3, 0);

    courseMapClasses.get(8).add(0, 1);
    courseMapClasses.get(8).add(1, 0);
    courseMapClasses.get(8).add(2, 0);
    courseMapClasses.get(8).add(3, 0);

    courseMapClasses.get(9).add(0, 0);
    courseMapClasses.get(9).add(1, 0);
    courseMapClasses.get(9).add(2, 0);
    courseMapClasses.get(9).add(3, 1);

    courseMapClasses.get(10).add(0, 2);
    courseMapClasses.get(10).add(1, 0);
    courseMapClasses.get(10).add(2, 12);
    courseMapClasses.get(10).add(3, 0);

    courseMapClasses.get(11).add(0, 0);
    courseMapClasses.get(11).add(1, 0);
    courseMapClasses.get(11).add(2, 12);
    courseMapClasses.get(11).add(3, 0);

    courseMapClasses.get(12).add(0, 0);
    courseMapClasses.get(12).add(1, 0);
    courseMapClasses.get(12).add(2, 1);
    courseMapClasses.get(12).add(3, 1);

    courseMapClasses.get(13).add(0, 2);
    courseMapClasses.get(13).add(1, 7);
    courseMapClasses.get(13).add(2, 0);
    courseMapClasses.get(13).add(3, 0);

    courseMapClasses.get(14).add(0, 1);
    courseMapClasses.get(14).add(1, 0);
    courseMapClasses.get(14).add(2, 2);
    courseMapClasses.get(14).add(3, 0);

    courseMapClasses.get(15).add(0, 1);
    courseMapClasses.get(15).add(1, 2);
    courseMapClasses.get(15).add(2, 0);
    courseMapClasses.get(15).add(3, 0);

    courseMapClasses.get(16).add(0, 1);
    courseMapClasses.get(16).add(1, 0);
    courseMapClasses.get(16).add(2, 0);
    courseMapClasses.get(16).add(3, 0);

    courseMapClasses.get(17).add(0, 0);
    courseMapClasses.get(17).add(1, 0);
    courseMapClasses.get(17).add(2, 1);
    courseMapClasses.get(17).add(3, 1);

    courseMapClasses.get(18).add(0, 1);
    courseMapClasses.get(18).add(1, 0);
    courseMapClasses.get(18).add(2, 1);
    courseMapClasses.get(18).add(3, 0);

    courseMapClasses.get(19).add(0, 0);
    courseMapClasses.get(19).add(1, 0);
    courseMapClasses.get(19).add(2, 0);
    courseMapClasses.get(19).add(3, 0);

    courseMapClasses.get(20).add(0, 0);
    courseMapClasses.get(20).add(1, 0);
    courseMapClasses.get(20).add(2, 0);
    courseMapClasses.get(20).add(3, 1);

    courseMapClasses.get(21).add(0, 1);
    courseMapClasses.get(21).add(1, 0);
    courseMapClasses.get(21).add(2, 0);
    courseMapClasses.get(21).add(3, 0);

    courseMapClasses.get(22).add(0, 1);
    courseMapClasses.get(22).add(1, 0);
    courseMapClasses.get(22).add(2, 2);
    courseMapClasses.get(22).add(3, 0);

    courseMapClasses.get(23).add(0, 1);
    courseMapClasses.get(23).add(1, 1);
    courseMapClasses.get(23).add(2, 5);
    courseMapClasses.get(23).add(3, 0);

    courseMapClasses.get(24).add(0, 1);
    courseMapClasses.get(24).add(1, 0);
    courseMapClasses.get(24).add(2, 6);
    courseMapClasses.get(24).add(3, 0);

    courseMapClasses.get(25).add(0, 1);
    courseMapClasses.get(25).add(1, 0);
    courseMapClasses.get(25).add(2, 0);
    courseMapClasses.get(25).add(3, 0);

    courseMapClasses.get(26).add(0, 1);
    courseMapClasses.get(26).add(1, 0);
    courseMapClasses.get(26).add(2, 0);
    courseMapClasses.get(26).add(3, 0);

    courseMapClasses.get(27).add(0, 0);
    courseMapClasses.get(27).add(1, 0);
    courseMapClasses.get(27).add(2, 1);
    courseMapClasses.get(27).add(3, 1);

    courseMapClasses.get(28).add(0, 1);
    courseMapClasses.get(28).add(1, 0);
    courseMapClasses.get(28).add(2, 2);
    courseMapClasses.get(28).add(3, 0);

    courseMapClasses.get(29).add(0, 1);
    courseMapClasses.get(29).add(1, 0);
    courseMapClasses.get(29).add(2, 2);
    courseMapClasses.get(29).add(3, 0);

    courseMapClasses.get(30).add(0, 1);
    courseMapClasses.get(30).add(1, 0);
    courseMapClasses.get(30).add(2, 2);
    courseMapClasses.get(30).add(3, 0);

    courseMapClasses.get(31).add(0, 1);
    courseMapClasses.get(31).add(1, 0);
    courseMapClasses.get(31).add(2, 0);
    courseMapClasses.get(31).add(3, 0);

    courseMapClasses.get(32).add(0, 1);
    courseMapClasses.get(32).add(1, 0);
    courseMapClasses.get(32).add(2, 2);
    courseMapClasses.get(32).add(3, 0);

    courseMapClasses.get(33).add(0, 1);
    courseMapClasses.get(33).add(1, 0);
    courseMapClasses.get(33).add(2, 3);
    courseMapClasses.get(33).add(3, 0);

    courseMapClasses.get(34).add(0, 1);
    courseMapClasses.get(34).add(1, 1);
    courseMapClasses.get(34).add(2, 0);
    courseMapClasses.get(34).add(3, 0);

    courseMapClasses.get(35).add(0, 1);
    courseMapClasses.get(35).add(1, 0);
    courseMapClasses.get(35).add(2, 2);
    courseMapClasses.get(35).add(3, 0);

    courseMapClasses.get(36).add(0, 1);
    courseMapClasses.get(36).add(1, 0);
    courseMapClasses.get(36).add(2, 3);
    courseMapClasses.get(36).add(3, 0);

    courseMapClasses.get(37).add(0, 0);
    courseMapClasses.get(37).add(1, 0);
    courseMapClasses.get(37).add(2, 3);
    courseMapClasses.get(37).add(3, 1);

    courseMapClasses.get(38).add(0, 1);
    courseMapClasses.get(38).add(1, 0);
    courseMapClasses.get(38).add(2, 0);
    courseMapClasses.get(38).add(3, 0);

    courseMapClasses.get(39).add(0, 1);
    courseMapClasses.get(39).add(1, 0);
    courseMapClasses.get(39).add(2, 0);
    courseMapClasses.get(39).add(3, 0);

    courseMapClasses.get(40).add(0, 1);
    courseMapClasses.get(40).add(1, 0);
    courseMapClasses.get(40).add(2, 1);
    courseMapClasses.get(40).add(3, 0);

    courseMapClasses.get(41).add(0, 1);
    courseMapClasses.get(41).add(1, 3);
    courseMapClasses.get(41).add(2, 0);
    courseMapClasses.get(41).add(3, 0);

    courseMapClasses.get(42).add(0, 1);
    courseMapClasses.get(42).add(1, 0);
    courseMapClasses.get(42).add(2, 1);
    courseMapClasses.get(42).add(3, 0);

    courseMapClasses.get(43).add(0, 1);
    courseMapClasses.get(43).add(1, 0);
    courseMapClasses.get(43).add(2, 0);
    courseMapClasses.get(43).add(3, 0);

    courseMapClasses.get(44).add(0, 2);
    courseMapClasses.get(44).add(1, 7);
    courseMapClasses.get(44).add(2, 0);
    courseMapClasses.get(44).add(3, 0);

    orientationStudents = new HashMap<Integer, Integer>();
    orientationStudents.put(0, 39);
    orientationStudents.put(1, 259);
    orientationStudents.put(2, 96);
    orientationStudents.put(3, 218);
    orientationStudents.put(4, 728);
    orientationStudents.put(5, 242);
    orientationStudents.put(6, 21);
    orientationStudents.put(7, 28);
    orientationStudents.put(8, 102);
    orientationStudents.put(9, 50);
    orientationStudents.put(10, 41);
    orientationStudents.put(11, 206);
    orientationStudents.put(12, 91);

    // factor of total students that take courses of the "key" year
    attendanceFactor = new HashMap<Integer, Float>();
    attendanceFactor.put(1, 0.882f);
    attendanceFactor.put(2, 0.475f);
    attendanceFactor.put(3, 0.307f);
    // special case for course 44, it can be coursed either in
    // the second year and in the third. Attendance factors are averaged
    attendanceFactor.put(0, (attendanceFactor.get(2) + attendanceFactor.get(3)) / 2);

    // maps a course to the year its dictated
    courseMapYear = new HashMap<Integer, Integer>();
    for (int courseId = 0; courseId < 45; courseId++) {
      if (courseId < 5) {
        courseMapYear.put(courseId, 1);
      } else if (courseId < 17) {
        courseMapYear.put(courseId, 2);
      } else if (courseId < 44) {
        courseMapYear.put(courseId, 3);
      } else {
        // special case for course 44
        courseMapYear.put(courseId, 0);
      }
    }

    classStudents = new HashMap<Integer, Integer>();
    for (int course = 0; course < 45; course++) {
      int students = 0;
      for (Integer orientation : orientationStudents.keySet()) {
        if (courseMapOrientation.get(course).contains(orientation)) {
          students += orientationStudents.get(orientation);
        }
      }
      int year = courseMapYear.get(course);
      students = (int) (students * attendanceFactor.get(year));
      classStudents.put(course, students);
    }
  }
}
