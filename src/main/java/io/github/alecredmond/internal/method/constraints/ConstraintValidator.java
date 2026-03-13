package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConstraintValidator {
  private final ProbabilityConstraint constraint;
  private final BayesianNetworkData data;

  public ConstraintValidator(ProbabilityConstraint constraint, BayesianNetworkData data) {
    this.constraint = constraint;
    this.data = data;
  }

  public boolean validate() {
    statesExistInNetwork();
    parametersAreUnique();
    probabilityWithinBounds();
    if (constraint instanceof MarginalConstraint mc) {
      noConditionsPresent(mc);
    }
    if (constraint instanceof ConditionalConstraint cc) {
      noConditionsInEventNode(cc);
    }
    return true;
  }

  private void statesExistInNetwork() {
    Set<NodeState> states = new HashSet<>(constraint.getAllStates());
    states.removeAll(data.getNodeStateIDsMap().values());
    if (states.isEmpty()) {
      return;
    }
    throw new ConstraintValidationException(
        "Some nodeStates in %s are not defined in the network!".formatted(constraint));
  }

  private void parametersAreUnique() {
    Set<NodeState> eventStates = constraint.getEventStates();
    Set<NodeState> conditionStates = constraint.getConditionStates();
    boolean parametersAreUnique =
        data.getConstraints().parallelStream()
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

  private void noConditionsPresent(MarginalConstraint mc) {
    if (mc.getConditionStates().isEmpty()) {
      return;
    }
    throw new ConstraintValidationException(
        "Marginal ProbabilityConstraint %s contained conditional states!".formatted(mc));
  }

  private void noConditionsInEventNode(ConditionalConstraint cc) {
    Node eventNode = cc.getEventNode();
    Set<Node> conditionNodes = cc.getConditionNodes();
    if (!conditionNodes.contains(eventNode)) {
      return;
    }
    throw new ConstraintValidationException(
        "ProbabilityConstraint %s is conditional upon itself!".formatted(cc));
  }
}
