package io.github.alecredmond.internal.method.constraints.handlers;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import java.util.Map;

public interface ConstraintHandler {

  double adjustAndReturnError();

  void updateResults(Map<ProbabilityConstraint, double[]> results);
}
