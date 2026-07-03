package io.github.alecredmond.internal.method.constraints.strategy;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;

public interface ValidatedConstraint<P extends ProbabilityConstraint> {
  P getConstraint();
}
