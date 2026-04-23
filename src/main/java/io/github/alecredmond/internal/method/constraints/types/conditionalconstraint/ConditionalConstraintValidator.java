package io.github.alecredmond.internal.method.constraints.types.conditionalconstraint;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.ConstraintValidator;

public class ConditionalConstraintValidator extends ConstraintValidator<ConditionalConstraint> {

  public ConditionalConstraintValidator() {
    super();
  }

  @Override
  public boolean validateInputs(ConstraintBuilderData data) {
    return data.getEventStates().size() == 1 && !data.getConditionStates().isEmpty();
  }

  @Override
  public Class<ConditionalConstraint> getConstraintClass() {
    return ConditionalConstraint.class;
  }

  @Override
  protected void constraintSpecificValidation() {
    // No extra requirements
  }

  @Override
  protected ConditionalConstraint constraintConstructorMethod(ConstraintBuilderData data) {
    return new ConditionalConstraint(
        data.getEventStates().stream().findAny().orElseThrow(),
        data.getConditionStates(),
        data.getProbability());
  }
}
