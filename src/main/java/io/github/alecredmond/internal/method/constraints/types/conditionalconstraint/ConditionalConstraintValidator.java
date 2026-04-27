package io.github.alecredmond.internal.method.constraints.types.conditionalconstraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.BaseConstraintValidator;
import java.util.Set;

public class ConditionalConstraintValidator extends BaseConstraintValidator<ConditionalConstraint>
    implements ConstraintValidator<ConditionalConstraint> {

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
    Node eventNode = constraint.getEventNode();
    Set<Node> conditionNodes = constraint.getConditionNodes();
    if (!conditionNodes.contains(eventNode)) {
      return;
    }
    throw new ConstraintValidationException(
        "ProbabilityConstraint %s is conditional upon itself!".formatted(constraint));
  }

  @Override
  protected ConditionalConstraint constraintConstructorMethod(ConstraintBuilderData data) {
    return new ConditionalConstraint(
        data.getEventStates().stream().findAny().orElseThrow(),
        data.getConditionStates(),
        data.getProbability());
  }
}
