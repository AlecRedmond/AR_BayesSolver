package io.github.alecredmond.internal.method.inference.junctiontree;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.inference.SolverResults;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.internal.application.inference.SolverConfigs;
import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolver;
import io.github.alecredmond.internal.method.inference.SolverResultsBuilder;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JTASolver {

  public SolverResults solveNetwork(BayesianNetworkData networkData, SolverConfigs configs) {
    boolean writeLogs = configs.isLogSolverProgress();
    Instant start = Instant.now();

    if (writeLogs) {
      log.info("STARTING SOLVER");
    }

    JunctionTreeAlgorithm jta = buildJTA(networkData,configs);

    double lastError;
    double error = Double.MAX_VALUE;
    double converge = Double.MAX_VALUE;

    long now = Instant.now().getEpochSecond();
    long endTime = now + configs.getTimeLimitSeconds();
    long nextLogTime = now + configs.getLogIntervalSeconds();

    Map<Clique, List<ConstraintSolver>> constraintMap = jta.getData().getConstraintHandlersMap();

    boolean thresholdReached = false;
    boolean timeLimitReached;
    int cycle;
    double[] adder = {0.0};

    for (cycle = 0; cycle < configs.getCyclesLimit(); cycle++) {
      lastError = error;
      error = runSolverCycleAndReturnError(jta, constraintMap,adder);
      converge = Math.abs(error - lastError);

      now = Instant.now().getEpochSecond();
      thresholdReached = converge <= configs.getConvergeThreshold();
      timeLimitReached = now >= endTime;

      if (thresholdReached || timeLimitReached) {
        break;
      }

      if (writeLogs && now >= nextLogTime) {
        nextLogTime = now + configs.getLogIntervalSeconds();
        logCycleComplete(cycle, converge, error);
      }
    }

    if (writeLogs) {
      logEndStatement(thresholdReached, start, Instant.now());
      logCycleComplete(cycle, converge, error);
    }

    jta.writeTablesToNetwork();
    return writeResults(constraintMap, cycle);
  }

  private JunctionTreeAlgorithm buildJTA(BayesianNetworkData networkData, SolverConfigs configs) {
    JunctionTreeAlgorithm jta = JunctionTreeAlgorithm.buildForSolver(networkData,configs);
    jta.marginalizeTables();
    return jta;
  }

  private double runSolverCycleAndReturnError(
          JunctionTreeAlgorithm jta, Map<Clique, List<ConstraintSolver>> constraintHandlers, double[] adder) {

    adder[0] = 0.0;

    constraintHandlers.forEach(
        (clique, handlers) ->
            handlers.forEach(
                h -> {
                  adder[0] += h.adjustAndReturnError();
                  jta.sumTransfer(clique);
                }));

    return adder[0];
  }

  private void logCycleComplete(int cycle, double loss, double error) {
    log.info(String.format("CYCLE %d : LOSS = %1.2e : ERROR = %1.2e", cycle, loss, error));
  }

  private void logEndStatement(boolean thresholdReached, Instant start, Instant end) {
    log.info(
        thresholdReached ? "SOLVER FOUND A SOLUTION IN {} ms" : "SOLVER TIMED OUT AFTER {} ms",
        end.toEpochMilli() - start.toEpochMilli());
  }

  private SolverResults writeResults(
      Map<Clique, List<ConstraintSolver>> constraintMap, int cycle) {
    Map<ProbabilityConstraint, double[]> resultsMap = new HashMap<>();
    constraintMap.values().stream()
        .flatMap(Collection::stream)
        .forEach(handler -> handler.updateResults(resultsMap));
    return new SolverResultsBuilder().buildResults(cycle, resultsMap);
  }
}
