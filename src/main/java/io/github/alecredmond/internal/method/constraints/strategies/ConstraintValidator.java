package io.github.alecredmond.internal.method.constraints.strategies;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;

public interface ConstraintValidator<T extends ProbabilityConstraint> {
  void validateForNetwork(ConstraintBuilderData data);

  boolean validateInputs(ConstraintBuilderData data);

  Class<T> getConstraintClass();

  void buildConstraint(ConstraintBuilderData data);
}
