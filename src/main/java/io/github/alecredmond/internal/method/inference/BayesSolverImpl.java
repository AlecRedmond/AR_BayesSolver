package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.method.inference.BayesSolver;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.inference.junctiontree.JTASolver;

public class BayesSolverImpl implements BayesSolver {
  private final BayesianNetwork network;

  public BayesSolverImpl(BayesianNetwork network) {
    this.network = network;
  }

  @Override
  public boolean solve() {
    BayesianNetworkData data = network.getNetworkData();
    if (data.isSolved()) return false;
    network.buildNetworkData();
    JTASolver.solveNetwork(data);
    data.setSolved(true);
    return true;
  }

  @Override
  public boolean isSolved() {
    return network.getNetworkData().isSolved();
  }
}
