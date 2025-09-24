package com.artools.method.solver;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.network.BayesNetData;
import com.artools.application.network.ProportionalFitterData;
import com.artools.application.solver.SolverConfigs;
import com.artools.method.probabilitytables.TableUtils;
import com.artools.method.solver.netsampler.JunctionTreeAlgorithm;
import com.artools.method.solver.netsampler.NetworkSampler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProportionalFitter {
  private final SolverConfigs configs;
  private final ProportionalFitterData data;

  public ProportionalFitter(BayesNetData netData, SolverConfigs configs) {
    this.configs = configs;
    this.data = ProportionalFitterDataBuilder.build(netData, configs);
  }

  public void solveNetwork() {
    // TODO - Fix the fact this doesn't work
    JunctionTreeAlgorithm jta = new JunctionTreeAlgorithm(data.getBayesNetData());
    double lastError;
    int percentileStep = configs.getCyclesLimit() / 100;
    for (int i = 0; i < configs.getCyclesLimit(); i++) {
      double error = data.getError();
      if (endSolverRun()) {
        logCycleComplete(i, data.getLoss(), error);
        break;
      }
      lastError = error;
      error = runCycle(jta);
      double loss = Math.abs(error - lastError);
      if (i % percentileStep == 0) logCycleComplete(i, loss, error);
      data.setLoss(loss);
      data.setError(error);
    }
    jta.writeTablesToNetwork();
  }

  private boolean endSolverRun() {
    return Math.abs(data.getLoss()) < configs.getConvergeThreshold();
  }

  private void logCycleComplete(int i, double loss, double error) {
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
