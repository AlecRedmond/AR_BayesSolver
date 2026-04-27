package io.github.alecredmond.internal.method.constraints.strategies;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ConstraintValidator<T extends ProbabilityConstraint> {
  protected BayesianNetworkData networkData;
  protected T constraint;

  protected ConstraintValidator() {
    this.networkData = null;
  }

  public abstract boolean validateInputs(ConstraintBuilderData data);

  public abstract Class<T> getConstraintClass();

  @SuppressWarnings("unchecked")
  public void validateForNetwork(ConstraintBuilderData data) {
    this.networkData = data.getNetworkData();
    this.constraint = (T) data.getConstraint();
    constraintSpecificValidation();
    statesExistInNetwork();
    noIdenticalConstraintsInNetwork();
  }

  protected abstract void constraintSpecificValidation() throws ConstraintValidationException;

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

  public static void validateCommon(ConstraintBuilderData data) {
    notConditionalOnSelf(data);
    noSharedConditionNodes(data);
    probabilityWithinBounds(data);
  }

  protected static void notConditionalOnSelf(ConstraintBuilderData data) {
    Set<Node> eventNodes = data.getEventNodes();
    Set<Node> conditionNodes = data.getConditionNodes();
    boolean noCrossover = eventNodes.stream().noneMatch(conditionNodes::contains);
    if (noCrossover) {
      return;
    }
    throw new ConstraintValidationException(
        "%s found events conditional upon themselves!".formatted(formatOutput(data)));
  }

  protected static void noSharedConditionNodes(ConstraintBuilderData data) {
    Set<NodeState> conditionStates = data.getConditionStates();
    Set<Node> conditionNodes = data.getConditionNodes();
    if (conditionStates.size() == conditionNodes.size()) {
      return;
    }
    throw new ConstraintValidationException("%s Found condition states sharing a node!");
  }

  protected static void probabilityWithinBounds(ConstraintBuilderData data) {
    double probability = data.getProbability();
    if (probability < 0) {
      throw new ConstraintValidationException(
          "%s has probability < 0".formatted(formatOutput(data)));
    } else if (probability > 1) {
      throw new ConstraintValidationException(
          "%s has probability > 1".formatted(formatOutput(data)));
    }
  }

  protected static String formatOutput(ConstraintBuilderData data) {
    Set<NodeState> events = data.getEventStates();
    Set<NodeState> conditions = data.getConditionStates();
    double prob = data.getProbability();
    if (conditions.isEmpty()) {
      return "P(%s) == %.3f".formatted(NodeUtils.formatStatesToString(events), prob);
    }
    return "P(%s|%s) == %.3f"
        .formatted(
            NodeUtils.formatStatesToString(events),
            NodeUtils.formatStatesToString(conditions),
            prob);
  }

  public void buildConstraint(ConstraintBuilderData data) {
    data.setConstraint(constraintConstructorMethod(data));
  }

  protected abstract T constraintConstructorMethod(ConstraintBuilderData data);
}
