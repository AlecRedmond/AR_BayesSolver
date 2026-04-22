package io.github.alecredmond.internal.method.inference.junctiontree;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.inference.SolverResults;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.internal.application.inference.SolverConfigs;
import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolverHandler;
import io.github.alecredmond.internal.method.inference.SolverResultsBuilder;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JTASolver {

  private JTASolver() {}

  public static SolverResults solveNetwork(BayesianNetworkData networkData) {
    SolverConfigs configs = new SolverConfigs();
    boolean writeLogs = configs.isLogSolverProgress();
    Instant start = Instant.now();

    if (writeLogs) {
      log.info("STARTING SOLVER");
    }

    JunctionTreeAlgorithm jta = buildJTA(networkData);

    double lastError;
    double error = Double.MAX_VALUE;
    double converge = Double.MAX_VALUE;

    long now = Instant.now().getEpochSecond();
    long endTime = now + configs.getTimeLimitSeconds();
    long nextLogTime = now + configs.getLogIntervalSeconds();

    Map<Clique, List<ConstraintSolverHandler<ProbabilityConstraint>>> constraintMap =
        jta.getData().getConstraintHandlersMap();

    boolean thresholdReached = false;
    boolean timeLimitReached;
    int cycle;

    for (cycle = 0; cycle < configs.getCyclesLimit(); cycle++) {
      lastError = error;
      error = runSolverCycleAndReturnError(jta, constraintMap);
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

  private static JunctionTreeAlgorithm buildJTA(BayesianNetworkData networkData) {
    JunctionTreeAlgorithm jta = JunctionTreeAlgorithm.buildForSolver(networkData);
    jta.marginalizeTables();
    return jta;
  }

  private static double runSolverCycleAndReturnError(
      JunctionTreeAlgorithm jta,
      Map<Clique, List<ConstraintSolverHandler<ProbabilityConstraint>>> constraintHandlers) {

    DoubleAdder cycleError = new DoubleAdder();

    constraintHandlers.forEach(
        (clique, handlers) ->
            handlers.forEach(
                h -> {
                  double error = h.adjustAndReturnError();
                  cycleError.add(error);
                  h.storeError(error);
                  jta.sumTransfer(clique);
                }));

    return cycleError.sum();
  }

  private static void logCycleComplete(int cycle, double loss, double error) {
    log.info(String.format("CYCLE %d : LOSS = %1.2e : ERROR = %1.2e", cycle, loss, error));
  }

  private static void logEndStatement(boolean thresholdReached, Instant start, Instant end) {
    log.info(
        thresholdReached ? "SOLVER FOUND A SOLUTION IN {} ms" : "SOLVER TIMED OUT AFTER {} ms",
        end.toEpochMilli() - start.toEpochMilli());
  }

  private static SolverResults writeResults(
      Map<Clique, List<ConstraintSolverHandler<ProbabilityConstraint>>> constraintMap, int cycle) {
    Map<ProbabilityConstraint, double[]> resultsMap = new HashMap<>();
    constraintMap.values().stream()
        .flatMap(Collection::stream)
        .forEach(handler -> handler.updateResults(resultsMap));
    return new SolverResultsBuilder().buildResults(cycle, resultsMap);
  }
}
