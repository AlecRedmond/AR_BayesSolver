package io.github.alecredmond.internal.method.constraints.types.marginalconstraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;

public class MarginalConstraintValidator extends ConstraintValidator<MarginalConstraint> {

  public MarginalConstraintValidator() {
    super();
  }

  @Override
  public Class<MarginalConstraint> getConstraintClass() {
    return MarginalConstraint.class;
  }

  @Override
  public void validateInputs(ConstraintBuilderData data) {
    boolean noConditions = data.getConditionStates().isEmpty();
    if (!noConditions) {
      throw new ConstraintValidationException("Marginal Constraints must not have conditions!");
    }
    boolean oneEventState = data.getEventStates().size() == 1;
    if (!oneEventState) {
      throw new ConstraintValidationException(
          "Marginal Constraints must have only one event state!");
    }
  }

  @Override
  protected MarginalConstraint constructConstraint(ConstraintBuilderData data) {
    return new MarginalConstraint(
        data.getEventStates().stream().findAny().orElseThrow(), data.getProbability());
  }
}
