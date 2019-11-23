package org.uma.jmetal.runner.scheduleHandler;

import org.uma.jmetal.problem.singleobjective.Schedule;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.solution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.scheduledata.ScheduleDataHandler;

public class ScheduleDataHandlerTest {
  public void main(String[] args) throws Exception {
    System.out.println("-----------------------------");
    System.out.println("TESTEANDO CLASE DATA HANDLER");
    System.out.println("-----------------------------");
    
    ScheduleDataHandler handler = new ScheduleDataHandler();
    Schedule problem = new Schedule(handler);
    IntegerSolution solution = new DefaultIntegerSolution(problem);
    
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
    if (handler.getTypeProportionInCourse(7, 1) == 1f/12){
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
    if (handler.hasPair(11, solution) == true) {
      System.out.println("OK");
    } else {
      System.out.println("ERROR! ESPERABA true PARA EL INDICE 11 Y OBTUVE " + handler.hasPair(11, solution));
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
  }
}