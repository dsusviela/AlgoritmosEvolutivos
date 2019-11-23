package org.uma.jmetal.runner.scheduleHandler;

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
    for (int i = 0; i < 1110; i++) {
      solution.setVariableValue(i, 0);
    }
    
    solution.setVariableValue(1, 11);
    solution.setVariableValue(11, 121);

    solution.setVariableValue(121, 11);
    solution.setVariableValue(131, 1);

    System.out.println("HANDLER DATA CARGADA");

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
    solution.setVariableValue(490, 480);
    IntegerSolution swappedSol = handler.swapFeasibleClassroom(1, 480, solution);
    if (swappedSol == null) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! SE ESPERABA QUE NO HUBIERA SWAP");
    }
    swappedSol = handler.swapFeasibleClassroom(840, 480, solution);
    if (swappedSol != null) {
      if (swappedSol.getVariableValue(840) == 11 &&
          swappedSol.getVariableValue(850) == 840 &&
          swappedSol.getVariableValue(480) == 0 &&
          swappedSol.getVariableValue(490) == 0) {
        System.out.println("OK");
      } else {
        System.out.println("ERROR! SE ESPERABA QUE EN EL LUGAR 840 ESTE EL 11 SE TIENE EL " + swappedSol.getVariableValue(840));
        System.out.println("ERROR! SE ESPERABA QUE EN EL LUGAR 850 ESTE EL 840 SE TIENE EL " + swappedSol.getVariableValue(850));
        System.out.println("ERROR! SE ESPERABA QUE EN EL LUGAR 480 ESTE EL 0 SE TIENE EL " + swappedSol.getVariableValue(480));
        System.out.println("ERROR! SE ESPERABA QUE EN EL LUGAR 490 ESTE EL 0 SE TIENE EL " + swappedSol.getVariableValue(490));
      }
    } else {
        System.out.println("ERROR! NO SE OBTUVO SWAP FACTIBLE");
    }
  }
}