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
  public boolean validateInputs(ConstraintBuilderData data) {
    boolean hasMultipleEvents = data.getEventStates().size() > 1;
    boolean overDifferentNodes = data.getEventNodes().size() == data.getEventStates().size();
    return hasMultipleEvents && overDifferentNodes;
  }

  @Override
  public Class<JointProbabilityConstraint> getConstraintClass() {
    return JointProbabilityConstraint.class;
  }

  @Override
  protected void constraintSpecificValidation() {
    if (constraint.getAllNodes().size() == constraint.getAllStates().size()) {
      return;
    }
    throw new ConstraintValidationException(
        "Constraint %s has multiple states sharing the same node!".formatted(constraint));
  }

  @Override
  protected JointProbabilityConstraint constraintConstructorMethod(ConstraintBuilderData data) {
    return new JointProbabilityConstraint(
        data.getEventStates(), data.getConditionStates(), data.getProbability());
  }
}
