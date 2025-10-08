package io.github.alecredmond.method.constraints;

import io.github.alecredmond.application.constraints.ConditionalConstraint;
import io.github.alecredmond.application.constraints.MarginalConstraint;
import io.github.alecredmond.application.constraints.ParameterConstraint;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.exceptions.ParameterConstraintBuilderException;
import java.util.Collection;
import java.util.List;

public class ConstraintBuilder {

  private ConstraintBuilder() {}

  public static <T, E> ParameterConstraint buildConstraint(
      Collection<T> eventStateIDs,
      Collection<E> conditionStateIDs,
      double probability,
      BayesianNetworkData data) {
    if (eventStateIDs.size() == 1) {
      return buildConstraint(
          eventStateIDs.stream().findAny().orElseThrow(), conditionStateIDs, probability, data);
    }
    List<NodeState> eventStates = getNodeStates(eventStateIDs, data);
    List<NodeState> conditionStates = getNodeStates(conditionStateIDs, data);
    return new ParameterConstraint(eventStates, conditionStates, probability);
  }

  public static <T, E> ParameterConstraint buildConstraint(
      T eventStateID,
      Collection<E> conditionStateIDs,
      double probability,
      BayesianNetworkData data) {
    NodeState eventState = getNodeState(eventStateID, data);
    List<NodeState> conditionStates = getNodeStates(conditionStateIDs, data);
    noConditionsInEventNode(eventState, conditionStates);
    if (conditionStates.isEmpty()) return new MarginalConstraint(eventState, probability);
    return new ConditionalConstraint(eventState, conditionStates, probability);
  }

  private static <E> List<NodeState> getNodeStates(
      Collection<E> conditionStateIDs, BayesianNetworkData data) {
    return conditionStateIDs.stream().map(sID -> data.getNodeStateIDsMap().get(sID)).toList();
  }

  private static <T> NodeState getNodeState(T eventStateID, BayesianNetworkData data) {
    return data.getNodeStateIDsMap().get(eventStateID);
  }

  private static void noConditionsInEventNode(
      NodeState eventState, List<NodeState> conditionStates) {
    Node node = eventState.getParentNode();
    boolean conditionsInEventNode =
        conditionStates.stream().anyMatch(state -> state.getParentNode().equals(node));
    if (conditionsInEventNode) {
      throw new ParameterConstraintBuilderException(
          String.format(
              "Constraint Builder found Condition States and Event States simultaneously on %s",
              node));
    }
  }

  public static <T> MarginalConstraint buildConstraint(
      T eventStateID, double probability, BayesianNetworkData data) {
    return new MarginalConstraint(getNodeState(eventStateID, data), probability);
  }
}
