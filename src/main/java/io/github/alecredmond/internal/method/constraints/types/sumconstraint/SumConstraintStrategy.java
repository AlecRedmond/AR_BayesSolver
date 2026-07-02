package io.github.alecredmond.internal.method.constraints.types.sumconstraint;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.constraints.SumProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintStrategy;
import lombok.Getter;

@Getter
public class SumConstraintStrategy implements ConstraintStrategy<SumProbabilityConstraint> {
  private final SumConstraintValidator constraintValidator;
  private final SumConstraintSerializer constraintSerializer;

  public SumConstraintStrategy() {
    constraintValidator = new SumConstraintValidator();
    constraintSerializer = new SumConstraintSerializer();
  }

  @Override
  public SumProbabilityConstraint safeCast(ProbabilityConstraint constraint) {
    if (constraint instanceof SumProbabilityConstraint spc) return spc;
    return null;
  }

  @Override
  public Class<SumProbabilityConstraint> constraintClass() {
    return SumProbabilityConstraint.class;
  }
}
