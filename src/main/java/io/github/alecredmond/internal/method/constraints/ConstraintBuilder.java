package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ConstraintBuilder {
  private static final List<? extends ConstraintValidator<?>> INPUT_VALIDATORS = init();
  private final ConstraintBuilderData data;

  public ConstraintBuilder(
      Set<NodeState> eventStates,
      Set<NodeState> conditionStates,
      double probability,
      BayesianNetworkData networkData) {
    this.data = new ConstraintBuilderData(networkData, eventStates, conditionStates, probability);
    commonLogic();
  }

  private void commonLogic() {
    try {
      ConstraintValidator.validateCommon(data);
      selectValidator();
      buildConstraint();
      validateConstraint();
    } catch (ConstraintValidationException e) {
      data.setException(e);
    }
  }

  private void selectValidator() {
    data.setValidator(
        INPUT_VALIDATORS.stream()
            .filter(v -> v.validateInputs(data))
            .findAny()
            .orElseThrow(
                () ->
                    new ConstraintValidationException(
                        "Could not match inputs P(%s|%s) to any ProbabilityConstraint type!"
                            .formatted(
                                NodeUtils.formatStatesToString(data.getEventStates()),
                                NodeUtils.formatStatesToString(data.getConditionStates())))));
  }

  private void buildConstraint() {
    data.getValidator().buildConstraint(data);
  }

  private void validateConstraint() {
    data.getValidator().validateForNetwork(data);
  }

  public ConstraintBuilder(ProbabilityConstraint constraint, BayesianNetworkData networkData) {
    this.data = buildConstraintDataForExisting(constraint, networkData);
    if (getException().isPresent()) return;
    preBuiltConstraintLogic();
  }

  private ConstraintBuilderData buildConstraintDataForExisting(
      ProbabilityConstraint constraint, BayesianNetworkData networkData) {
    return Optional.ofNullable(
            ConstraintRegistry.getStrategy(constraint.getClass()).buildConstraintValidator())
        .map(v -> new ConstraintBuilderData(networkData, constraint, v))
        .orElse(
            new ConstraintBuilderData(
                networkData,
                constraint,
                new ConstraintValidationException(
                    "Could not find a validator for constraint %s".formatted(constraint))));
  }

  public Optional<ConstraintValidationException> getException() {
    return Optional.ofNullable(data.getException());
  }

  private void preBuiltConstraintLogic() {
    try {
      validateConstraint();
    } catch (ConstraintValidationException e) {
      data.setException(e);
    }
  }

  public ConstraintBuilder(
      @NonNull NodeState event,
      Set<NodeState> conditions,
      double probability,
      BayesianNetworkData networkData) {
    this.data = new ConstraintBuilderData(networkData, Set.of(event), conditions, probability);
    commonLogic();
  }

  public ConstraintBuilder(
      @NonNull NodeState event, double probability, BayesianNetworkData networkData) {
    this.data = new ConstraintBuilderData(networkData, Set.of(event), Set.of(), probability);
    commonLogic();
  }

  public ProbabilityConstraint getConstraint() {
    return data.getConstraint();
  }

  private static List<? extends ConstraintValidator<?>> init() {
    return Arrays.stream(ConstraintTypes.values())
        .map(
            type ->
                ConstraintRegistry.getStrategy(type.getConstraintClass())
                    .buildConstraintValidator())
        .toList();
  }
}
