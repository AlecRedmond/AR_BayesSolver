package io.github.alecredmond.internal.method.constraints.strategies;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;

public interface CPTConstraintValidator<P extends ProbabilityConstraint> {
  void validateCPTConstraint(P constraint);

  Class<P> getConstraintClass();
}
