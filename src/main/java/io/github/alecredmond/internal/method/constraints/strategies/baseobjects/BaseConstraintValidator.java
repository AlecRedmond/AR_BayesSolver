package io.github.alecredmond.internal.method.constraints.strategies.baseobjects;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseConstraintValidator<T extends ProbabilityConstraint> {
  protected BayesianNetworkData networkData;
  protected T constraint;

  protected BaseConstraintValidator() {
    this.networkData = null;
  }

  @SuppressWarnings("unchecked")
  public void validateForNetwork(ConstraintBuilderData data) {
    this.networkData = data.getNetworkData();
    this.constraint = (T) data.getConstraint();
    probabilityWithinBounds();
    statesExistInNetwork();
    noIdenticalConstraintsInNetwork();
    constraintSpecificValidation();
  }

  private void probabilityWithinBounds() {
    double probability = constraint.getProbability();
    if (probability < 0) {
      throw new ConstraintValidationException(
          "ProbabilityConstraint %s has probability < 0".formatted(constraint.toString()));
    } else if (probability > 1) {
      throw new ConstraintValidationException(
          "ProbabilityConstraint %s has probability > 1".formatted(constraint.toString()));
    }
  }

  private void statesExistInNetwork() {
    Set<NodeState> states = new HashSet<>(constraint.getAllStates());
    states.removeAll(networkData.getNodeStateIDsMap().values());
    if (states.isEmpty()) {
      return;
    }
    throw new ConstraintValidationException(
        "NodeStates %s in %s are not defined in the network!"
            .formatted(NodeUtils.formatStatesToString(states), constraint));
  }

  protected void noIdenticalConstraintsInNetwork() {
    Set<NodeState> eventStates = constraint.getEventStates();
    Set<NodeState> conditionStates = constraint.getConditionStates();
    boolean parametersAreUnique =
        networkData.getConstraints().parallelStream()
            .filter(c -> eventStates.equals(c.getEventStates()))
            .noneMatch(c -> conditionStates.equals(c.getConditionStates()));
    if (parametersAreUnique) {
      return;
    }
    throw new ConstraintValidationException(
        String.format(
            "Attempted to add a probabilityConstraint C(%s | %s), which already exists!",
            NodeUtils.formatStatesToString(eventStates),
            NodeUtils.formatStatesToString(conditionStates)));
  }

  protected abstract void constraintSpecificValidation() throws ConstraintValidationException;

  public void buildConstraint(ConstraintBuilderData data) {
    data.setConstraint(constraintConstructorMethod(data));
  }

  protected abstract T constraintConstructorMethod(ConstraintBuilderData data);
}
