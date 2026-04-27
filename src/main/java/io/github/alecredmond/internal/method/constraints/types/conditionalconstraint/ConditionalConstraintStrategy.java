package io.github.alecredmond.internal.method.constraints.types.conditionalconstraint;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSerializer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;

public class ConditionalConstraintStrategy implements ConstraintStrategy<ConditionalConstraint> {

  @Override
  public ConditionalConstraintValidator buildConstraintValidator() {
    return new ConditionalConstraintValidator();
  }

  @Override
  public ConstraintSerializer<ConditionalConstraint> buildConstraintSerializer() {
    return new ConditionalConstraintSerializer();
  }
}
