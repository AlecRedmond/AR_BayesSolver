package io.github.alecredmond.internal.method.constraints.strategies;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;
import java.util.Map;

public interface ConstraintSolverHandler<T extends ProbabilityConstraint> extends VectorIterator {

  double adjustAndReturnError();

  void updateResults(Map<ProbabilityConstraint, double[]> results);

  void storeError(double error);
}
