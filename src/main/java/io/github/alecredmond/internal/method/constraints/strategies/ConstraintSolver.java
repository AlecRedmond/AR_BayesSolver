package io.github.alecredmond.internal.method.constraints.strategies;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.application.inference.junctiontree.Clique;

import java.util.Map;
import java.util.Set;

public interface ConstraintSolver {
  double adjustAndReturnError();

  void updateResults(Map<ProbabilityConstraint, double[]> results, int cycle, Set<Clique> cliques);
}
