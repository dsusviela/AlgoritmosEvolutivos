package org.uma.jmetal.problem.singleobjective;

import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;

import java.util.LinkedList;
import java.util.List;

public class Schedule extends AbstractIntegerProblem {
  private int classes;

  public Schedule() {
    setNumberOfVariables(2);
    setNumberOfObjectives(1);
    setName("Schedule");
  }

  @Override
  public void evaluate(IntegerSolution solution) {
    LinkedList<Integer> solutionVector = new LinkedList<Integer>();
    solutionVector.add(solution.getVariableValue(0));
    solutionVector.add(solution.getVariableValue(1));
    int res = 0;
    for (Integer item : solutionVector) {
      res += item.intValue();
    }
    solution.setObjective(0, res);
  }
}
