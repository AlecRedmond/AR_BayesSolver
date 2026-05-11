package io.github.alecredmond.internal.method.constraints.types.combinedconstraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.application.constraints.SumProbabilityConstraint;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;
import io.github.alecredmond.internal.method.node.NodeUtils;

public class SumConstraintValidator extends ConstraintValidator<SumProbabilityConstraint> {
  @Override
  public boolean validateInputs(ConstraintBuilderData data) {
    boolean hasMultipleEvents = data.getEventStates().size() > 1;
    boolean withSharedNodes = data.getEventNodes().size() < data.getEventStates().size();
    return hasMultipleEvents && withSharedNodes;
  }

  @Override
  public Class<SumProbabilityConstraint> getConstraintClass() {
    return SumProbabilityConstraint.class;
  }

  @Override
  protected void constraintSpecificValidation() throws ConstraintValidationException {
    try {
      NodeUtils.generateRequest(constraint.getEventStates());
    } catch (NodeStateConflictException e) {
      return;
    }
    throw new ConstraintValidationException(
        "There are no event NodeStates sharing a state in %s!".formatted(constraint));
  }

  @Override
  protected SumProbabilityConstraint constraintConstructorMethod(ConstraintBuilderData data) {
    return new SumProbabilityConstraint(
        data.getEventStates(), data.getConditionStates(), data.getProbability());
  }
}
