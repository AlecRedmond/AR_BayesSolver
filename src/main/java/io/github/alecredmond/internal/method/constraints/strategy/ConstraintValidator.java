package io.github.alecredmond.internal.method.constraints.strategy;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;

public interface ConstraintValidator<
    P extends ProbabilityConstraint, V extends ValidatedConstraint<P>> {
  V validateConstraint(ProbabilityConstraint constraint, BayesianNetworkData data);

  V validateConstraint(ConstraintBuilderData data);

  Class<P> getConstraintClass();

  boolean checkInputsValid(ConstraintBuilderData data);

  V buildFromInputs(ConstraintBuilderData data);
}
