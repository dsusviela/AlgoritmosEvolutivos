package org.uma.jmetal.runner.singleobjective;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SinglePointCrossover;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.operator.impl.selection.TournamentSelection;
import org.uma.jmetal.problem.IntegerProblem;
import org.uma.jmetal.problem.singleobjective.Schedule;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import java.util.ArrayList;
import java.util.List;

public class GenerationalGeneticAlgorithmScheduleRunner {
  public void main(String[] args) throws Exception {
    IntegerProblem scheduleProblem;
    Algorithm<IntegerSolution> algorithm;
    CrossoverOperator<IntegerSolution> crossover;
    MutationOperator<IntegerSolution> mutator;
    SelectionOperator<List<IntegerSolution>, IntegerSolution> selector;

    scheduleProblem = new Schedule();
        /*
        String[] params = readFromFile('route/to/params.txt')
        problem = new Schdeule('route/to/problemPrams.txt');

        crossover = new ScheduleCrossover();

        mutator = new ScheduleMutator(params[mutation]);
        */
    crossover = new SinglePointCrossover(0.9);

    selector = new BinaryTournamentSelection<IntegerSolution>();

    algorithm = new GeneticAlgorithmBuilder<>(scheduleProblem, crossover, mutator)
        .setPopulationSize(100)
        .setMaxEvaluations(250000)
        .setSelectionOperator(selectior)
        .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    IntegerSolution solution = algorithm.getResult() ;
    List<IntegerSolution> population = new ArrayList<>(1) ;
    population.add(solution);

    long computingTime = algorithmRunner.getComputingTime() ;

    new SolutionListOutput(population)
        .setSeparator("\t")
        .setVarFileOutputContext(new DefaultFileOutputContext("Variables.tsv"))
        .setFunFileOutputContext(new DefaultFileOutputContext("Objectives.tsv"))
        .print();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    JMetalLogger.logger.info("Objectives values have been written to file Objectives.tsv");
    JMetalLogger.logger.info("Variables values have been written to file Variables.tsv");
  }
}
