package org.uma.jmetal.operator.impl.crossover;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.solution.IntegerSolution;

import java.util.List;

public class ScheduleCrossover implements CrossoverOperator<IntegerSolution> {
  @Override
  public int getNumberOfRequiredParents() {
    return 0;
  }

  @Override
  public int getNumberOfGeneratedChildren() {
    return 0;
  }

  @Override
  public List<IntegerSolution> execute(List<IntegerSolution> integerSolutions) {
    return null;
  }
}
