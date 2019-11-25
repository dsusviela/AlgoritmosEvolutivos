package org.uma.jmetal.runner.scheduleHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.uma.jmetal.problem.singleobjective.Schedule;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.solution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.scheduledata.ScheduleDataHandler;

public class ScheduleDataHandlerTest {
  public static void main(String[] args) throws Exception {
    System.out.println("-----------------------------");
    System.out.println("TESTEANDO CLASE DATA HANDLER");
    System.out.println("-----------------------------");
    
    ScheduleDataHandler handler = new ScheduleDataHandler();
    Schedule problem = new Schedule(handler);
    IntegerSolution solution = new DefaultIntegerSolution(problem);
    for (int i = 0; i < 2880; i++) {
      solution.setVariableValue(i, 0);
    }
    
    solution.setVariableValue(1, 11);
    solution.setVariableValue(11, 121);

    solution.setVariableValue(121, 11);
    solution.setVariableValue(131, 1);

    System.out.println("");
    System.out.println("DATOS CARGADOS");

    System.out.println("");
    System.out.println("-------------");
    System.out.println("PROBANDO FUNCIONES DE AYUDA");
    System.out.println("-------------");

    System.out.println("Probando la funcion getClassCourse");
    if (handler.getClassCourse(31) == 3) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! ESPERABA 3 OBTUVE " + handler.getClassCourse(31));
    }
    
    System.out.println("Probando la funcion getClassType");
    if (handler.getClassType(402) == 2){
      System.out.println("OK");
    } else {
      System.out.println("ERROR! ESPERABA 2 OBTUVE " + handler.getClassType(402));
    }
    
    System.out.println("Probando la funcion getTypeProportionInCourse");
    if (handler.getTypeProportionInCourse(1, 7) == 1f/12){
      System.out.println("OK");
    } else {
      System.out.println("ERROR! ESPERABA 0.083 OBTUVE " + handler.getTypeProportionInCourse(7, 1));
    }
    
    System.out.println("Probando la funcion getClassroom");
    if (handler.getClassroom(254) == 4) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! ESPERABA 4 OBTUVE " + handler.getClassroom(254));
    }
    
    System.out.println("Probando la funcion hasPair");
    if (handler.hasPair(1, solution) == true) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! ESPERABA true PARA EL INDICE 1 Y OBTUVE " + handler.hasPair(11, solution));
    }
    if (handler.hasPair(126, solution) == false) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! ESPERABA true PARA EL INDICE 126 Y OBTUVE " + handler.hasPair(126, solution));
    }
    
    System.out.println("Probando la funcion indexHasClass");
    if (handler.indexHasClass(121, solution) == true) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! ESPERABA true  PARA EL INDICE 121 Y OBTUVE " + handler.indexHasClass(121, solution));
    }
    if (handler.indexHasClass(1000, solution) == false) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! ESPERABA false PARA EL INDICE 1000 Y OBTUVE " + handler.indexHasClass(1000, solution));
    }
    
    System.out.println("Probando la funcion getDay");
    if (handler.getDay(9) == 4) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! ESPERABA 4 OBTUVE " + handler.getDay(9));
    }
    
    System.out.println("Probando la funcion getTurn");
    if (handler.getTurn(27) == 1) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! ESPERABA 1 OBTUVE " + handler.getTurn(27));
    }
    
    System.out.println("Probando la funcion isIndexClass");
    if (handler.isIndexClass(16) == false) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! ESPERABA false PARA EL INDICE 16 OBTUVE " + handler.isIndexClass(16));
    }
    if (handler.isIndexClass(6) == true) {
        System.out.println("OK");
      } else {
        System.out.println("ERROR! ESPERABA true PARA EL INDICE 6 OBTUVE " + handler.isIndexClass(6));
      }
    
    System.out.println("Probando la funcion distanceBetweenDays");
    if (handler.distanceBetweenDays(0, 3) == 2) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! ESPERABA 2 OBTUVE " + handler.distanceBetweenDays(0, 3));
    }

    System.out.println("Probando la funcion getAttendingStudents");
    if (handler.getAttendingStudents(01) == 139) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! ESPERABA 2 PARA LA CLASE 01 OBTUVE " + handler.getAttendingStudents(01));
    }

    System.out.println("---------");
    System.out.println("FIN DE TEST DE FUNCIONES COMUNES");
    System.out.println("---------");
    System.out.println("");

    System.out.println("---------");
    System.out.println("COMIENZO DE TEST DE FUNCIONES DE FACTIBILIDAD");
    System.out.println("---------");

    System.out.println("testeando funcion de victimas para un turno y dia fijo");
    int attendingStudents = handler.getAttendingStudents(solution.getVariableValue(1));
    solution.setVariableValue(480, 11);
    solution.setVariableValue(481, 21);
    if (handler.getVictimSetTurnDay(1, attendingStudents, solution).contains(480) &&
        handler.getVictimSetTurnDay(1, attendingStudents, solution).contains(481)) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! LOS INDICES ESPERADOS ERAN 480 Y 481 OBTUVE " + handler.getVictimSetTurnDay(1, attendingStudents, solution));
    }

    System.out.println("testeando funcion de seleccion de victimas");
    if (handler.chooseVictim(handler.getVictimSetTurnDay(1, attendingStudents, solution), solution) == 480) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! LA VICTIMA ESPERADA ES 480 Y OBTUVE " + handler.chooseVictim(handler.getVictimSetTurnDay(1, attendingStudents, solution), solution));
    }

    System.out.println("testeando funcion de swapeo si es uno factible");
    // pair added to 480 cell
    solution.setVariableValue(180, 42);
    solution.setVariableValue(190, 480);
    solution.setVariableValue(490, 180);
    // new cells added for swap
    solution.setVariableValue(840, 21);
    solution.setVariableValue(850, 840);
    IntegerSolution swappedSol = handler.swapFeasibleClassroom(840, 480, solution);
    if (swappedSol != null) {
      if (swappedSol.getVariableValue(840) == 11 &&
          swappedSol.getVariableValue(850) == 180 &&
          swappedSol.getVariableValue(480) == 21 &&
          swappedSol.getVariableValue(490) == 480) {
        System.out.println("OK");
      } else {
        System.out.println("ERROR! SE ESPERABA QUE EN EL LUGAR 840 ESTE EL 11 SE TIENE EL " + swappedSol.getVariableValue(840));
        System.out.println("ERROR! SE ESPERABA QUE EN EL LUGAR 850 ESTE EL 180 SE TIENE EL " + swappedSol.getVariableValue(850));
        System.out.println("ERROR! SE ESPERABA QUE EN EL LUGAR 480 ESTE EL 21 SE TIENE EL " + swappedSol.getVariableValue(480));
        System.out.println("ERROR! SE ESPERABA QUE EN EL LUGAR 490 ESTE EL 480 SE TIENE EL " + swappedSol.getVariableValue(490));
      }
    } else {
        System.out.println("ERROR! NO SE OBTUVO SWAP FACTIBLE");
    }

    System.out.println("testeando funcion getCandidateDaysForPair");
    HashSet<Integer> expectedDays = new HashSet<Integer>();
    expectedDays.add(2);
    expectedDays.add(3);
    expectedDays.add(4);
    if (handler.getCandidateDaysForPair(01, solution).containsAll(expectedDays)) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! LOS DIAS ESPERADOS SON 2, 3, 4 SE OBTUVIERON " + handler.getCandidateDaysForPair(01, solution));
    }
    expectedDays = new HashSet<Integer>();
    solution.setVariableValue(482, 11);
    expectedDays.add(3);
    expectedDays.add(4);
    if (handler.getCandidateDaysForPair(482, solution).containsAll(expectedDays)) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! LOS DIAS ESPERADOS SON 3, 4 SE OBTUVIERON " + handler.getCandidateDaysForPair(482, solution));
    }
    solution.setVariableValue(482, 0);

    expectedDays = new HashSet<Integer>();
    solution.setVariableValue(488, 11);
    expectedDays.add(0);
    expectedDays.add(1);
    expectedDays.add(2);
    if (handler.getCandidateDaysForPair(488, solution).containsAll(expectedDays)) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! LOS DIAS ESPERADOS SON 0, 1, 2 SE OBTUVIERON " + handler.getCandidateDaysForPair(488, solution));
    }
    solution.setVariableValue(488, 0);

    System.out.println("testeando funcion getVictimSetTurnClassroom");
    solution.setVariableValue(488, 01);
    solution.setVariableValue(485, 21);
    solution.setVariableValue(388, 01);
    solution.setVariableValue(500, 01);
    solution.setVariableValue(488, 11);
    HashSet<Integer> expectedClasses = new HashSet<Integer>();
    expectedClasses.add(488);
    expectedClasses.add(485);
    if (handler.getVictimSetTurnClassroom(480, solution).containsAll(expectedClasses)) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! LOS INDICES ESPERADOS SON 488, 485 SE OBTUVIERON " + handler.getVictimSetTurnClassroom(480, solution));
    }

    // reseting solution for next tests
    for (int i = 0; i < 2880; i++) {
      solution.setVariableValue(i, 0);
    }

    System.out.println("testeando funcion getFeasibleClassroomsNoPair");
    HashMap<Integer, ArrayList<Integer>> dataClass10 = handler.getFeasibleClassroomsNoPair(10, solution);
    attendingStudents = handler.getAttendingStudents(10);
    if (dataClass10.size() == 30) {
      System.out.println("CANTIDAD DE OPCIONES: OK");
    } else {
      System.out.println("ERROR! SE ESPERABAN 60 OPCIONES Y SE OBTUVIERON " + dataClass10.size());
    }
    System.out.println("testeando correctitud de las " + dataClass10.size() + " opciones");
    for (ArrayList<Integer> option : dataClass10.values()) {
      if (attendingStudents <= handler.getClassroomCapacity(option.get(0))) {
        int cell = 60*option.get(0) + 20*option.get(1) + 2*option.get(2);
        if (solution.getVariableValue(cell) == 0) {
          System.out.println("OPCION " + option + " : OK");
        } else {
          System.out.println("ERROR! LA OPCION " + option + " INTENTA LA CELDA " + cell + " LA CUAL ESTA OCUPADA");
        }
      } else  {
        System.out.println("ERROR! LA OPCION " + option + " CONSIDERA UN SALON CON CAPACIDAD INSUFICIENTE (" + 
            handler.getClassroomCapacity(option.get(0)) + ") PARA LOS " + attendingStudents);
      }
    }

    System.out.println("testeando funcion getFeasibleClassroomsWithPair");
    dataClass10 = handler.getFeasibleClassroomsWithPair(10, solution);
    attendingStudents = handler.getAttendingStudents(10);
    if (dataClass10.size() == 72) {
      System.out.println("CANTIDAD DE OPCIONES: OK");
    } else {
      System.out.println("ERROR! SE ESPERABAN 120 OPCIONES Y SE OBTUVIERON " + dataClass10.size());
    }
    System.out.println("testeando correctitud de las " + dataClass10.size() + " opciones");
    for (ArrayList<Integer> option : dataClass10.values()) {
      if (attendingStudents <= handler.getClassroomCapacity(option.get(0))) {
        int cell = 60*option.get(0) + 20*option.get(1) + 2*option.get(2);
        if (solution.getVariableValue(cell) == 0) {
          int pairCell = 60*option.get(0) + 20*option.get(1) + 2*option.get(3);
          if (solution.getVariableValue(pairCell) != 0) {
            System.out.println("ERROR! LA OPCION " + option + " INTENTA LA CELDA " + pairCell + " PARA EL PAR LA CUAL ESTA OCUPADA");
          }
        } else {
          System.out.println("ERROR! LA OPCION " + option + " INTENTA LA CELDA " + cell + " LA CUAL ESTA OCUPADA");
        }
      } else  {
        System.out.println("ERROR! LA OPCION " + option + " CONSIDERA UN SALON CON CAPACIDAD INSUFICIENTE (" + 
            handler.getClassroomCapacity(option.get(0)) + ") PARA LOS " + attendingStudents);
      }
    }
    
    System.out.println("testeando la generacion de instancias");
    solution = problem.createSolution();
    if (solution == null) {
      System.out.println("ERROR! EN LA GENERACION AZAROSA DE UNA SOLUCION (null)");
    }
    System.out.println("se genero una instancia, chequeando que esten todas las clases en la instancia");
    HashMap<Integer, HashMap<Integer, Integer>> courseHeatMap = new HashMap<Integer, HashMap<Integer, Integer>>();
    for (Integer course : handler.getCourseMapClasses().keySet()) {
      courseHeatMap.put(course, new HashMap<Integer, Integer>());
      for (int type = 0; type < 4; type++) {
        courseHeatMap.get(course).put(type, 0);
      }
    }
    for (int cellIndex = 0; cellIndex < handler.getCellsInMatrix(); cellIndex++) {
      int classType = handler.getClassType(solution.getVariableValue(cellIndex));
      int classCourse = handler.getClassCourse(solution.getVariableValue(cellIndex));
      int previousValue = courseHeatMap.get(classCourse).get(classType);
      HashMap<Integer, Integer> aux = courseHeatMap.get(classCourse);
      aux.put(classType, previousValue++);
      courseHeatMap.put(classCourse, aux);
    }

    HashMap<Integer, ArrayList<Integer>> courseMapClasses = handler.getCourseMapClasses();
    for (Integer course : courseMapClasses.keySet()) {
      for (int type = 0; type < 4; type++) {
        if (handler.getCourseMapClasses().get(course).get(type) == courseHeatMap.get(course).get(type)) {

        } else  {
          int difference = handler.getCourseMapClasses().get(course).get(type) - courseHeatMap.get(course).get(type);
          System.out.println("ERROR! FALTAN " + difference + " CLASES DE " + course + " DEL TPO " + type);
        }
      }
    }

  }
}