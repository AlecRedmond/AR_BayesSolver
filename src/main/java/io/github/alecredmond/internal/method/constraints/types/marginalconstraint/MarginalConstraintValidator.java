package io.github.alecredmond.internal.method.constraints.types.marginalconstraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.BaseConstraintValidator;

public class MarginalConstraintValidator extends BaseConstraintValidator<MarginalConstraint>
    implements ConstraintValidator<MarginalConstraint> {

  public MarginalConstraintValidator() {
    super();
  }

  @Override
  public boolean validateInputs(ConstraintBuilderData data) {
    return data.getConditionStates().isEmpty() && data.getEventStates().size() == 1;
  }

  @Override
  public Class<MarginalConstraint> getConstraintClass() {
    return MarginalConstraint.class;
  }

  @Override
  protected void constraintSpecificValidation() {
    boolean noConditions = constraint.getConditionStates().isEmpty();
    if (!noConditions) {
      throw new ConstraintValidationException(
          "Marginal ProbabilityConstraint %s contained conditional states!%n"
              .formatted(constraint));
    }
  }

  @Override
  protected MarginalConstraint constraintConstructorMethod(ConstraintBuilderData data) {
    return new MarginalConstraint(
        data.getEventStates().stream().findAny().orElseThrow(), data.getProbability());
  }
}
