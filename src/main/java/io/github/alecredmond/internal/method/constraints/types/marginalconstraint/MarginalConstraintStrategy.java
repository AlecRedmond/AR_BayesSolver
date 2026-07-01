package io.github.alecredmond.internal.method.constraints.types.marginalconstraint;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintStrategy;
import lombok.Getter;

@Getter
public class MarginalConstraintStrategy implements ConstraintStrategy<MarginalConstraint> {
  private final MarginalConstraintValidator constraintValidator;
  private final MarginalConstraintSerializer constraintSerializer;

  public MarginalConstraintStrategy() {
    constraintSerializer = new MarginalConstraintSerializer();
    constraintValidator = new MarginalConstraintValidator();
  }

  @Override
  public MarginalConstraint safeCast(ProbabilityConstraint constraint) {
    if (constraint instanceof MarginalConstraint mc) return mc;
    return null;
  }

  @Override
  public Class<MarginalConstraint> constraintClass() {
    return MarginalConstraint.class;
  }
}
