package io.github.alecredmond.internal.method.constraints.types.combinedconstraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.SumProbabilityConstraint;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;

public class SumConstraintValidator extends ConstraintValidator<SumProbabilityConstraint> {
  @Override
  public Class<SumProbabilityConstraint> getConstraintClass() {
    return SumProbabilityConstraint.class;
  }

  @Override
  public void validateInputs(ConstraintBuilderData data) {
    boolean hasMultipleEvents = data.getEventStates().size() > 1;
    if (!hasMultipleEvents) {
      throw new ConstraintValidationException("A Sum Constraint must have multiple events!");
    }
    boolean withSharedNodes = data.getEventNodes().size() < data.getEventStates().size();
    if (!withSharedNodes) {
      throw new ConstraintValidationException(
          "A Sum Constraint must include at least 1 shared node in the event states!");
    }
  }

  @Override
  protected SumProbabilityConstraint constructConstraint(ConstraintBuilderData data) {
    return new SumProbabilityConstraint(
        data.getEventStates(), data.getConditionStates(), data.getProbability());
  }
}
