package io.github.alecredmond.method.constraints;

import io.github.alecredmond.application.constraints.ConditionalConstraint;
import io.github.alecredmond.application.constraints.Constraint;
import io.github.alecredmond.application.constraints.MarginalConstraint;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.method.node.NodeUtils;
import java.util.HashSet;
import java.util.Set;

public class ConstraintValidator {

  private ConstraintValidator() {}

  public static void validate(Constraint constraint, BayesianNetworkData data) {
    statesExistInNetwork(constraint, data);
    parametersAreUnique(constraint, data);
    probabilityWithinBounds(constraint);
    if (constraint instanceof MarginalConstraint mc) {
      noConditionsPresent(mc);
    }
    if (constraint instanceof ConditionalConstraint cc) {
      noConditionsInEventNode(cc);
    }
  }

  private static void statesExistInNetwork(Constraint constraint, BayesianNetworkData data) {
    Set<NodeState> states = new HashSet<>(constraint.getAllStates());
    states.removeAll(data.getNodeStateIDsMap().values());
    if (states.isEmpty()) {
      return;
    }
    throw new ConstraintValidationException(
        "Some nodeStates in %s are not defined in the network!".formatted(constraint));
  }

  private static void parametersAreUnique(Constraint constraint, BayesianNetworkData networkData) {
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
            "Attempted to add a constraint C(%s | %s), which already exists!",
            NodeUtils.formatToString(eventStates), NodeUtils.formatToString(conditionStates)));
  }

  private static void probabilityWithinBounds(Constraint constraint) {
    double probability = constraint.getProbability();
    if (probability < 0) {
      throw new ConstraintValidationException(
          "Constraint %s has probability < 0".formatted(constraint.toString()));
    } else if (probability > 1) {
      throw new ConstraintValidationException(
          "Constraint %s has probability > 1".formatted(constraint.toString()));
    }
  }

  private static void noConditionsPresent(MarginalConstraint mc) {
    if (mc.getConditionStates().isEmpty()) {
      return;
    }
    throw new ConstraintValidationException(
        "Marginal Constraint %s contained conditional states!".formatted(mc));
  }

  private static void noConditionsInEventNode(ConditionalConstraint cc) {
    Node eventNode = cc.getEventNode();
    Set<Node> conditionNodes = cc.getConditionNodes();
    if (!conditionNodes.contains(eventNode)) {
      return;
    }
    throw new ConstraintValidationException(
        "Constraint %s is conditional upon itself!".formatted(cc));
  }
}
