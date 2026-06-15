package io.github.alecredmond.internal.method.constraints.types.jointconstraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.JointProbabilityConstraint;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;

public class JointConstraintValidator extends ConstraintValidator<JointProbabilityConstraint> {

  public JointConstraintValidator() {
    super();
  }

  @Override
  public Class<JointProbabilityConstraint> getConstraintClass() {
    return JointProbabilityConstraint.class;
  }

  @Override
  public void validateInputs(ConstraintBuilderData data) {
    boolean hasMultipleEvents = data.getEventStates().size() > 1;
    if (!hasMultipleEvents) {
      throw new ConstraintValidationException("A joint constraint must have multiple events!");
    }
    boolean overDifferentNodes = data.getEventNodes().size() == data.getEventStates().size();
    if (!overDifferentNodes) {
      throw new ConstraintValidationException(
          "A joint constraint cannot have event states sharing the same node!");
    }
  }

  @Override
  protected JointProbabilityConstraint constructConstraint(ConstraintBuilderData data) {
    return new JointProbabilityConstraint(
        data.getEventStates(), data.getConditionStates(), data.getProbability());
  }
}
