package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.export.application.inference.SolverResults;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.method.inference.BayesSolver;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.application.inference.SolverConfigs;
import io.github.alecredmond.internal.method.inference.junctiontree.JTASolver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BayesSolverImpl implements BayesSolver {
  private final BayesianNetwork network;
  private final SolverConfigs configs;
  private SolverResults results;

  public BayesSolverImpl(BayesianNetwork network) {
    this.network = network;
    this.results = null;
    this.configs = new SolverConfigs();
  }

  @Override
  public boolean solve() {
    if (isSolved()) {
      return true;
    }
    return forceSolve();
  }

  @Override
  public boolean solve(SolverType solverType) {
    if (isSolved()) {
      return true;
    }
    return forceSolve(solverType);
  }

  @Override
  public boolean forceSolve(SolverType solverType) {
    configs.updateConfigs();
    configs.setSolverType(solverType);
    return forceSolveCommon();
  }

  @Override
  public boolean isSolved() {
    return network.isSolved();
  }

  @Override
  public SolverResults getResults() {
    if (results == null) {
      log.warn("NO RESULTS FROM CURRENT SOLVER");
    }
    return results;
  }

  @Override
  public boolean forceSolve() {
    configs.updateConfigs();
    return forceSolveCommon();
  }

  private boolean forceSolveCommon() {
    BayesianNetworkData data = network.getNetworkData();
    data.setSolved(false);
    results = null;
    try {
      network.buildNetworkData();
      results = new JTASolver().solveNetwork(data, configs);
      data.setSolved(true);
      return true;
    } catch (Exception e) {
      log.error(e.getLocalizedMessage(), e);
      return false;
    }
  }
}
