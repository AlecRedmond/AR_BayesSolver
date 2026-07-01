package io.github.alecredmond.internal.method.constraints.types.combinedconstraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.constraints.SumProbabilityConstraint;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.constraints.base.ConstraintValidatorBase;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintValidator;

public class SumConstraintValidator
    extends ConstraintValidatorBase<SumProbabilityConstraint, ValidatedSumConstraint>
    implements ConstraintValidator<SumProbabilityConstraint, ValidatedSumConstraint> {
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
  protected SumProbabilityConstraint safeCastConstraint(ProbabilityConstraint constraint) {
    if (constraint instanceof SumProbabilityConstraint spc) return spc;
    return null;
  }

  @Override
  protected ValidatedSumConstraint validatedConstraintConstructor(
      SumProbabilityConstraint constraint) {
    return new ValidatedSumConstraint(
        constraint.getEventStates(), constraint.getConditionStates(), constraint.getProbability());
  }

  @Override
  public Class<SumProbabilityConstraint> getConstraintClass() {
    return SumProbabilityConstraint.class;
  }

  @Override
  protected SumProbabilityConstraint constructConstraint(ConstraintBuilderData data) {
    return new SumProbabilityConstraint(
        data.getEventStates(), data.getConditionStates(), data.getProbability());
  }
}
