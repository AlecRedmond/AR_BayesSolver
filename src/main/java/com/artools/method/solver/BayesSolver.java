package com.artools.method.solver;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.network.BayesianNetworkData;
import com.artools.application.solver.SolverConfigs;
import com.artools.method.sampler.jtasampler.JunctionTreeAlgorithm;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BayesSolver {
  private final SolverConfigs configs;
  private final BayesianNetworkData data;

  public BayesSolver(BayesianNetworkData bayesianNetworkData, SolverConfigs configs) {
    this.configs = configs;
    this.data = bayesianNetworkData;
  }

  public void solveNetwork() {
    log.info("STARTING SOLVER");
    JunctionTreeAlgorithm jta = new JunctionTreeAlgorithm(data);
    log.info("JUNCTION TREE BUILT");

    double lastError;
    double error = Double.MAX_VALUE;
    double converge = Double.MAX_VALUE;

    long now = Instant.now().getEpochSecond();
    long nextLogTime = now + configs.getLogIntervalSeconds();
    long endTime = now + configs.getTimeLimitSeconds();

    for (int i = 0; i < configs.getCyclesLimit(); i++) {
      if (checkEndCycles(i, converge, endTime)) {
        logCycleComplete(i - 1, converge, error, nextLogTime, true, true);
        break;
      }

      lastError = error;
      error = runCycle(jta);
      converge = Math.abs(error - lastError);

      nextLogTime = logCycleComplete(i, converge, error, nextLogTime, false, false);
    }
    jta.writeTablesToNetwork();
  }

  private boolean checkEndCycles(int i, double converge, long endTime) {
    if (converge <= configs.getConvergeThreshold()) return true;
    if (i >= configs.getCyclesLimit()) return true;
    return Instant.now().getEpochSecond() >= endTime;
  }

  private long logCycleComplete(
      int i,
      double loss,
      double error,
      long nextLogTime,
      boolean solverRunComplete,
      boolean converged) {
    long now = Instant.now().getEpochSecond();
    if (checkSkipLog(now, nextLogTime, solverRunComplete)) return nextLogTime;
    if (converged) log.info("!!SOLUTION FOUND!!");
    String output = String.format("CYCLE %d : LOSS = %1.2e : ERROR = %1.2e", i, loss, error);
    log.info(output);
    return now + configs.getLogIntervalSeconds();
  }

  private double runCycle(JunctionTreeAlgorithm jta) {
    double error = 0.0;
    for (ParameterConstraint constraint : data.getConstraints()) {
      error += jta.adjustAndReturnError(constraint);
    }
    return error;
  }

  private boolean checkSkipLog(long now, long nextLogTime, boolean solverRunComplete) {
    if (solverRunComplete) return false;
    return now < nextLogTime;
  }
}
