package io.github.alecredmond.internal.method.constraints.strategies;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import java.util.Map;

public interface ConstraintSolver {
  double adjustAndReturnError();

  void updateResults(Map<ProbabilityConstraint, double[]> results);
}
