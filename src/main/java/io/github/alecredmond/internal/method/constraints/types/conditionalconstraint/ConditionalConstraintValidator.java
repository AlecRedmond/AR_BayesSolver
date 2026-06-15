package io.github.alecredmond.internal.method.constraints.types.conditionalconstraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;

public class ConditionalConstraintValidator extends ConstraintValidator<ConditionalConstraint> {

  public ConditionalConstraintValidator() {
    super();
  }

  @Override
  public Class<ConditionalConstraint> getConstraintClass() {
    return ConditionalConstraint.class;
  }

  @Override
  public void validateInputs(ConstraintBuilderData data) {
    if (data.getEventStates().size() != 1) {
      throw new ConstraintValidationException("Event State Size Must Equal 1!");
    }
    if (data.getConditionStates().isEmpty()) {
      throw new ConstraintValidationException("Conditional Constraints must have conditions!");
    }
  }

  @Override
  protected ConditionalConstraint constructConstraint(ConstraintBuilderData data) {
    return new ConditionalConstraint(
        data.getEventStates().stream().findAny().orElseThrow(),
        data.getConditionStates(),
        data.getProbability());
  }
}
