package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.export.application.inference.SolverResults;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.method.inference.BayesSolver;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.inference.junctiontree.JTASolver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BayesSolverImpl implements BayesSolver {
  private final BayesianNetwork network;
  private SolverResults results;

  public BayesSolverImpl(BayesianNetwork network) {
    this.network = network;
    this.results = null;
  }

  @Override
  public boolean solve() {
    if (isSolved()) {
      return true;
    }
    return forceSolve();
  }

  @Override
  public boolean isSolved() {
    return network.getNetworkData().isSolved();
  }

  @Override
  public boolean forceSolve() {
    BayesianNetworkData data = network.getNetworkData();
    data.setSolved(false);
    results = null;
    try {
      network.buildNetworkData();
      results = JTASolver.solveNetwork(data);
      data.setSolved(true);
      return true;
    } catch (Exception e) {
      log.error(e.getMessage());
      return false;
    }
  }

  @Override
  public SolverResults getResults() {
    if (results == null) {
      log.warn("NO RESULTS FROM CURRENT SOLVER");
    }
    return results;
  }
}
