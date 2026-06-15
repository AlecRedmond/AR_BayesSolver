package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.List;
import java.util.Set;

public class ConstraintFactory {
  private final BayesianNetworkData networkData;

  @SuppressWarnings("rawtypes")
  private final List<ConstraintValidator> validators;

  public ConstraintFactory(BayesianNetworkData networkData) {
    this.networkData = networkData;
    this.validators = ConstraintRegistry.buildValidatorList();
  }

  public ConstraintBuilderData buildConstraint(
      Set<NodeState> eventStates, Set<NodeState> conditionStates, double probability) {
    ConstraintBuilderData cbd =
        new ConstraintBuilderData(networkData, eventStates, conditionStates, probability);
    selectValidatorAndBuild(cbd);
    return cbd;
  }

  private void selectValidatorAndBuild(ConstraintBuilderData cbd) {
    validators.stream()
        .filter(v -> v.checkInputsValid(cbd))
        .findFirst()
        .ifPresentOrElse(v -> v.buildFromInputs(cbd), () -> addValidatorRefusedException(cbd));
  }

  private void addValidatorRefusedException(ConstraintBuilderData cbd) {
    cbd.setException(
        new ConstraintValidationException(
            "Could not match inputs P(%s|%s) to any ProbabilityConstraint type!"
                .formatted(
                    NodeUtils.formatStatesToString(cbd.getEventStates()),
                    NodeUtils.formatStatesToString(cbd.getConditionStates()))));
  }

  public <T extends ProbabilityConstraint> ConstraintBuilderData verifyConstraint(T constraint) {
    ConstraintBuilderData cbd = new ConstraintBuilderData(networkData, constraint);
    try {
      getValidator(constraint).verifyConstraint(cbd);
    } catch (ConstraintValidationException e) {
      cbd.setException(e);
    }
    return cbd;
  }

  @SuppressWarnings("unchecked")
  private <T extends ProbabilityConstraint> ConstraintValidator<T> getValidator(T constraint) {
    return validators.stream()
        .filter(cv -> cv.getConstraintClass().equals(constraint.getClass()))
        .findAny()
        .map(cv -> (ConstraintValidator<T>) cv)
        .orElseThrow(
            () ->
                new ConstraintValidationException(
                    "NO VALIDATOR FOR CONSTRAINT CLASS %s"
                        .formatted(constraint.getClass().getName())));
  }
}
