package io.github.alecredmond.internal.method.constraints.types.combinedconstraint;

import io.github.alecredmond.export.application.constraints.SumProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSerializer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;

public class SumConstraintStrategy implements ConstraintStrategy<SumProbabilityConstraint> {

  @Override
  public ConstraintValidator<SumProbabilityConstraint> buildConstraintValidator() {
    return new SumConstraintValidator();
  }

  @Override
  public ConstraintSerializer<SumProbabilityConstraint> buildConstraintSerializer() {
    return new SumConstraintSerializer();
  }
}
