package io.github.alecredmond.method.inference.junctiontree;

import io.github.alecredmond.application.constraints.ParameterConstraint;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.inference.InferenceEngineConfigs;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JTASolver {

  private JTASolver() {}

  public static void solveNetwork(BayesianNetworkData data, InferenceEngineConfigs configs) {
    log.info("STARTING SOLVER");
    data.setSolved(false);
    JunctionTreeAlgorithm jta = new JunctionTreeAlgorithm(data);
    log.info("JUNCTION TREE BUILT");

    double lastError;
    double error = Double.MAX_VALUE;
    double converge = Double.MAX_VALUE;

    long now = Instant.now().getEpochSecond();
    long nextLogTime = now + configs.getSolverLogIntervalSeconds();
    long endTime = now + configs.getSolverTimeLimitSeconds();

    for (int cycle = 0; cycle < configs.getSolverCyclesLimit(); cycle++) {
      if (checkEndCycles(cycle, converge, endTime, configs)) {
        logCycleComplete(configs, cycle - 1, converge, error, nextLogTime, true, true);
        break;
      }

      lastError = error;
      error = runSolverCycleAndReturnError(jta, data);
      converge = Math.abs(error - lastError);

      nextLogTime = logCycleComplete(configs, cycle, converge, error, nextLogTime, false, false);
    }

    jta.writeTablesToNetwork();
  }

  private static boolean checkEndCycles(
      int i, double converge, long endTime, InferenceEngineConfigs configs) {
    if (converge <= configs.getSolverConvergeThreshold()) return true;
    if (i >= configs.getSolverCyclesLimit()) return true;
    return Instant.now().getEpochSecond() >= endTime;
  }

  private static long logCycleComplete(
      InferenceEngineConfigs configs,
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
    return now + configs.getSolverLogIntervalSeconds();
  }

  private static double runSolverCycleAndReturnError(
      JunctionTreeAlgorithm jta, BayesianNetworkData data) {
    double cycleError = 0.0;
    for (ParameterConstraint constraint : data.getConstraints()) {
      cycleError += jta.adjustAndReturnError(constraint);
    }
    return cycleError;
  }

  private static boolean checkSkipLog(long now, long nextLogTime, boolean solverRunComplete) {
    if (solverRunComplete) return false;
    return now < nextLogTime;
  }
}
