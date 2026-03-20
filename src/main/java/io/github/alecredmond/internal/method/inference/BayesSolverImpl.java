package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.export.application.inference.SolverResults;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.method.inference.BayesSolver;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.inference.junctiontree.JTASolver;

public class BayesSolverImpl implements BayesSolver {
  private final BayesianNetwork network;
  private SolverResults results;

  public BayesSolverImpl(BayesianNetwork network) {
    this.network = network;
    this.results = null;
  }

  @Override
  public boolean solve() {
    BayesianNetworkData data = network.getNetworkData();
    if (data.isSolved()) return false;
    network.buildNetworkData();
    results = JTASolver.solveNetwork(data);
    data.setSolved(true);
    return true;
  }

  @Override
  public boolean isSolved() {
    return network.getNetworkData().isSolved();
  }

  @Override
  public SolverResults getResults() {
    return results;
  }
}
