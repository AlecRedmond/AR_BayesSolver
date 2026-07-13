package io.github.alecredmond.internal.method.constraints.types.jointconstraint;

import io.github.alecredmond.export.constraints.JointProbabilityConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedJointProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintStrategy;
import lombok.Getter;

@Getter
public class JointConstraintStrategy implements ConstraintStrategy<JointProbabilityConstraint> {
  private final JointConstraintValidator constraintValidator;
  private final JointConstraintSerializer constraintSerializer;

  public JointConstraintStrategy() {
    constraintValidator = new JointConstraintValidator();
    constraintSerializer = new JointConstraintSerializer();
  }

  @Override
  public boolean constraintIsInstance(ProbabilityConstraint constraint) {
    return constraint instanceof JointProbabilityConstraint;
  }

  @Override
  public boolean serializedIsInstance(SerializedProbabilityConstraint serialized) {
    return serialized instanceof SerializedJointProbabilityConstraint;
  }

  @Override
  public <P extends ProbabilityConstraint> JointProbabilityConstraint safeCastConstraint(
      P constraint) {
    if (constraint instanceof JointProbabilityConstraint jpc) return jpc;
    return null;
  }
}
