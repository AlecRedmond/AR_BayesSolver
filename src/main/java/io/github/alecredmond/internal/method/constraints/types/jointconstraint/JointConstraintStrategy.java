package io.github.alecredmond.internal.method.constraints.types.jointconstraint;

import io.github.alecredmond.export.application.constraints.JointProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSerializer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.ConstraintValidator;

public class JointConstraintStrategy implements ConstraintStrategy<JointProbabilityConstraint> {

  @Override
  public ConstraintValidator<JointProbabilityConstraint> buildConstraintValidator() {
    return new JointConstraintValidator();
  }

  @Override
  public ConstraintSerializer<JointProbabilityConstraint> buildConstraintSerializer() {
    return new JointConstraintSerializer();
  }
}
