package com.artools.method.solver;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.network.BayesNetData;
import com.artools.application.solver.SolverConfigs;
import com.artools.method.junctiontree.JunctionTreeAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BayesSolver {
  private final SolverConfigs configs;
  private final BayesNetData data;

  public BayesSolver(BayesNetData bayesNetData, SolverConfigs configs) {
    this.configs = configs;
    this.data = bayesNetData;
  }

  public void solveNetwork() {
    System.out.println("STARTING SOLVER");
    JunctionTreeAlgorithm jta = new JunctionTreeAlgorithm(data);
    System.out.println("JTA BUILT");
    double lastError;
    double error = Double.MAX_VALUE;
    double converge = Double.MAX_VALUE;
    int percentileStep = configs.getCyclesLimit() / 100;

    for (int i = 0; i < configs.getCyclesLimit(); i++) {
      if (converge < configs.getConvergeThreshold()) {
        logCycleComplete(i - 1, converge, error, true);
        break;
      }

      lastError = error;
      error = runCycle(jta);
      converge = Math.abs(error - lastError);

      logCycleComplete(i, converge, error, false);
    }
    jta.writeTablesToNetwork();
  }

  private void logCycleComplete(int i, double loss, double error, boolean solverRunComplete) {
    if (solverRunComplete) System.out.println("!!SOLUTION FOUND!!");
    String output =
        String.format(
            "CYCLE %d : LOSS = %1.2e : ERROR = %1.2e : STEP_SIZE = %1.2e",
            i, loss, error, configs.getStepSize());
    log.info(output);
    System.out.println(output);
  }

  private double runCycle(NetworkSampler sampler) {
    double error = 0.0;
    for (ParameterConstraint constraint : data.getConstraints()) {
      error += sampler.adjustAndReturnError(constraint);
    }
    return error;
  }
}
