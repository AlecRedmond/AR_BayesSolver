package io.github.alecredmond.internal.method.constraints.types.jointconstraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.JointProbabilityConstraint;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.BaseConstraintValidator;

public class JointConstraintValidator extends BaseConstraintValidator<JointProbabilityConstraint>
    implements ConstraintValidator<JointProbabilityConstraint> {

  public JointConstraintValidator() {
    super();
  }

  @Override
  public boolean validateInputs(ConstraintBuilderData data) {
    return data.getEventStates().size() > 1;
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
