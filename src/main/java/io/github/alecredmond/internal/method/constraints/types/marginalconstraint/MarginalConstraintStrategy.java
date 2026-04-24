package io.github.alecredmond.internal.method.constraints.types.marginalconstraint;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.ConstraintValidator;

public class MarginalConstraintStrategy implements ConstraintStrategy<MarginalConstraint> {

  @Override
  public ConstraintValidator<MarginalConstraint> buildConstraintValidator() {
    return new MarginalConstraintValidator();
  }

  @Override
  public MarginalConstraintSerializer buildConstraintSerializer() {
    return new MarginalConstraintSerializer();
  }
}
