package io.github.alecredmond.export.method.inference;

import io.github.alecredmond.export.application.inference.SolverResults;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.inference.solver.BayesSolverImpl;

public interface BayesSolver {
  static BayesSolver create(BayesianNetwork network) {
    return new BayesSolverImpl(network);
  }

  boolean solve();

  boolean solve(SolverType solverType);

  boolean forceSolve();

  boolean forceSolve(SolverType solverType);

  boolean isSolved();

  SolverResults getResults();

  enum SolverType {
    JOINT_TABLE_IPFP,
    JUNCTION_TREE_IPFP
  }
}
