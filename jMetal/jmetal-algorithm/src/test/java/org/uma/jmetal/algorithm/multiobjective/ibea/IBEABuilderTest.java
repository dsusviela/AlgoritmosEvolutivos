package org.uma.jmetal.algorithm.multiobjective.ibea;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Antonio J. Nebro on 25/11/14.
 */
public class IBEABuilderTest {
  private IBEABuilder<DoubleSolution> builder;
  private Problem<DoubleSolution> problem;
  private static final double EPSILON = 0.000000000000001;
  private static final int NUMBER_OF_VARIABLES_OF_THE_MOCKED_PROBLEM = 20;
  private CrossoverOperator<DoubleSolution> crossover;
  private MutationOperator<DoubleSolution> mutation;

  @SuppressWarnings("unchecked")
  @Before
  public void startup() {
    problem = mock(Problem.class);
    when(problem.getNumberOfVariables()).thenReturn(NUMBER_OF_VARIABLES_OF_THE_MOCKED_PROBLEM);

    double crossoverProbability = 0.9 ;
    double crossoverDistributionIndex = 20.0 ;
    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex) ;

    double mutationProbability = 1.0 / problem.getNumberOfVariables() ;
    double mutationDistributionIndex = 20.0 ;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex) ;

    builder = new IBEABuilder<>(problem, crossover, mutation);
  }

  @After
  public void cleanup() {
    problem = null;
    builder = null;
  }

  @Test
  public void buildAlgorithm() {
    IBEA<DoubleSolution> algorithm = builder.build();
    assertNotNull(algorithm);
  }

  @Test public void getProblem() {
    assertEquals(problem, builder.getProblem());
  }

  @Test public void testDefaultConfiguration() {
    assertEquals(100, builder.getPopulationSize());
    assertEquals(25000, builder.getMaxEvaluations());

    SBXCrossover crossover = (SBXCrossover) builder.getCrossover();
    assertEquals(0.9, crossover.getCrossoverProbability(), EPSILON);
    assertEquals(20.0, crossover.getDistributionIndex(), EPSILON);

    PolynomialMutation mutation = (PolynomialMutation) builder.getMutation();
    assertEquals(1.0 / NUMBER_OF_VARIABLES_OF_THE_MOCKED_PROBLEM, mutation.getMutationProbability(),
            EPSILON);
    assertEquals(20.0, mutation.getDistributionIndex(), EPSILON);
  }

  @Test public void setPositiveMaxNumberOfIterations() {
    builder.setMaxEvaluations(20000);
    assertEquals(20000, builder.getMaxEvaluations());
  }

  @Test public void setNewSelectionOperator() {
    @SuppressWarnings("unchecked")
    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection = mock(SelectionOperator.class);
    assertNotEquals(selection, builder.getSelection());
    builder.setSelection(selection);
    assertEquals(selection, builder.getSelection());
  }

}
