package io.github.alecredmond.method.inference.junctiontree;

import io.github.alecredmond.application.inference.SolverConfigs;
import io.github.alecredmond.application.inference.junctiontree.Clique;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.method.inference.InferenceEngine;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTAConstraintHandler;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JTASolver {

  private JTASolver() {}

  public static void solveNetwork(InferenceEngine engine) {
    BayesianNetworkData networkData = engine.getNetworkData();
    SolverConfigs configs = new SolverConfigs();
    Instant start = Instant.now();
    log.info("STARTING SOLVER");
    networkData.setSolved(false);
    JunctionTreeAlgorithm jta =
        new JunctionTreeAlgorithm(JTAInitializer.buildSolverConfiguration(networkData));
    jta.marginalizeTables();

    double lastError;
    double error = Double.MAX_VALUE;
    double converge = Double.MAX_VALUE;

    long now = Instant.now().getEpochSecond();
    long nextLogTime = now + configs.getLogIntervalSeconds();
    long endTime = now + configs.getTimeLimitSeconds();

    Clique[] cliqueArray = jta.getData().getCliqueSet().toArray(new Clique[0]);
    List<JTAConstraintHandler> constraintHandlers =
        jta.getData().getConstraintHandlers().values().stream().toList();

    for (int cycle = 0; cycle < configs.getCyclesLimit(); cycle++) {
      if (checkEndCycles(cycle, converge, endTime, configs)) {
        logCycleComplete(configs, cycle - 1, converge, error, nextLogTime, true, true);
        break;
      }

      lastError = error;
      error = runSolverCycleAndReturnError(jta, cycle, cliqueArray, constraintHandlers);
      converge = Math.abs(error - lastError);

      nextLogTime = logCycleComplete(configs, cycle, converge, error, nextLogTime, false, false);
    }

    jta.writeTablesToNetwork();
    Instant end = Instant.now();
    long msDuration = end.toEpochMilli() - start.toEpochMilli();
    log.info("Solver completed in {} ms", msDuration);
  }

  private static boolean checkEndCycles(
      int i, double converge, long endTime, SolverConfigs configs) {
    if (converge <= configs.getConvergeThreshold()) return true;
    if (i >= configs.getCyclesLimit()) return true;
    return Instant.now().getEpochSecond() >= endTime;
  }

  private static long logCycleComplete(
      SolverConfigs configs,
      int cycle,
      double loss,
      double error,
      long nextLogTime,
      boolean solverRunComplete,
      boolean converged) {
    long now = Instant.now().getEpochSecond();
    if (checkSkipLog(now, nextLogTime, solverRunComplete)) return nextLogTime;
    if (converged) log.info("!!SOLUTION FOUND!!");
    String output = String.format("CYCLE %d : LOSS = %1.2e : ERROR = %1.2e", cycle, loss, error);
    log.info(output);
    return now + configs.getLogIntervalSeconds();
  }

  private static double runSolverCycleAndReturnError(
      JunctionTreeAlgorithm jta,
      int cycle,
      Clique[] cliqueArray,
      List<JTAConstraintHandler> constraintHandlers) {
    double error = 0.0;
    for (JTAConstraintHandler handler : constraintHandlers) {
      error += handler.adjustAndReturnError();
    }

    Clique distributeFrom = cliqueArray[cycle % cliqueArray.length];
    jta.distributeAndCollectMessages(distributeFrom);

    return error;
  }

  private static boolean checkSkipLog(long now, long nextLogTime, boolean solverRunComplete) {
    if (solverRunComplete) return false;
    return now < nextLogTime;
  }
}
