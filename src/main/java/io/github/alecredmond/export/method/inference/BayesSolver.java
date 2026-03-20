package io.github.alecredmond.export.method.inference;

import io.github.alecredmond.export.application.inference.SolverResults;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.inference.BayesSolverImpl;

public interface BayesSolver {
  static BayesSolver create(BayesianNetwork network) {
    return new BayesSolverImpl(network);
  }

  boolean solve();

  boolean isSolved();

  SolverResults getResults();
}
