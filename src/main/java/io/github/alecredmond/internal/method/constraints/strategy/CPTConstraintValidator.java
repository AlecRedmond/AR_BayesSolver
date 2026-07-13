package io.github.alecredmond.internal.method.constraints.strategy;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;

public interface CPTConstraintValidator<
        P extends ProbabilityConstraint, V extends ValidatedConstraint<P>>
    extends ConstraintValidator<P, V> {
  V validateCPTConstraint(P constraint);
}
