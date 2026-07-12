package io.github.alecredmond.internal.method.constraints.types.conditionalconstraint;

import io.github.alecredmond.export.constraints.ConditionalConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintStrategy;
import lombok.Getter;

@Getter
public class ConditionalConstraintStrategy implements ConstraintStrategy<ConditionalConstraint> {
  private final ConditionalConstraintValidator constraintValidator;
  private final ConditionalConstraintSerializer constraintSerializer;

  public ConditionalConstraintStrategy() {
    this.constraintValidator = new ConditionalConstraintValidator();
    this.constraintSerializer = new ConditionalConstraintSerializer();
  }

    @Override
  public ConditionalConstraint safeCastConstraint(ProbabilityConstraint constraint) {
    if (constraint instanceof ConditionalConstraint cc) return cc;
    return null;
  }

}
