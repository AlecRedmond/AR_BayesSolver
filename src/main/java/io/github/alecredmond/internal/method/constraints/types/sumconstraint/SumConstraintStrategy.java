package io.github.alecredmond.internal.method.constraints.types.sumconstraint;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.constraints.SumProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedSumConstraint;
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
  public boolean constraintIsInstance(ProbabilityConstraint constraint) {
    return constraint instanceof SumProbabilityConstraint;
  }

  @Override
  public boolean serializedIsInstance(SerializedProbabilityConstraint serialized) {
    return serialized instanceof SerializedSumConstraint;
  }

  @Override
  public SumProbabilityConstraint safeCastConstraint(ProbabilityConstraint constraint) {
    if (constraint instanceof SumProbabilityConstraint spc) return spc;
    return null;
  }
}
