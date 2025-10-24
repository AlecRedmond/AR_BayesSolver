package io.github.alecredmond.method.constraints;

import io.github.alecredmond.application.constraints.ConditionalConstraint;
import io.github.alecredmond.application.constraints.MarginalConstraint;
import io.github.alecredmond.application.constraints.ParameterConstraint;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.exceptions.ParameterConstraintBuilderException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConstraintBuilder {

  private ConstraintBuilder() {}

  public static <T, E> ParameterConstraint buildConstraint(
      T eventStateID,
      Collection<E> conditionStateIDs,
      double probability,
      BayesianNetworkData data) {
    checkStatesExist(eventStateID, conditionStateIDs, data);
    checkProbabilityIsValid(probability);
    NodeState eventState = getNodeState(eventStateID, data);
    List<NodeState> conditionStates = getNodeStates(conditionStateIDs, data);
    noConditionsInEventNode(eventState, conditionStates);
    if (conditionStates.isEmpty()) return new MarginalConstraint(eventState, probability);
    return new ConditionalConstraint(eventState, conditionStates, probability);
  }

  private static <T, E> void checkStatesExist(
      T eventStateID, Collection<E> conditionStateIDs, BayesianNetworkData data) {
    Set<Object> tSet = new HashSet<>(conditionStateIDs);
    tSet.add(eventStateID);
    checkStatesExist(tSet, data);
  }

  private static void checkProbabilityIsValid(double probability) {
    if (probability < 0) {
      throw new ParameterConstraintBuilderException(
          "Attempted to build a constraint with probability < 0");
    } else if (probability > 1) {
      throw new ParameterConstraintBuilderException(
          "Attempted to build a constraint with probability > 1");
    }
  }

  private static <T> NodeState getNodeState(T eventStateID, BayesianNetworkData data) {
    return data.getNodeStateIDsMap().get(eventStateID);
  }

  private static <E> List<NodeState> getNodeStates(
      Collection<E> conditionStateIDs, BayesianNetworkData data) {
    return conditionStateIDs.stream().map(sID -> data.getNodeStateIDsMap().get(sID)).toList();
  }

  private static void noConditionsInEventNode(
      NodeState eventState, List<NodeState> conditionStates) {
    Node node = eventState.getNode();
    boolean conditionsInEventNode =
        conditionStates.stream().anyMatch(state -> state.getNode().equals(node));
    if (conditionsInEventNode) {
      throw new ParameterConstraintBuilderException(
          String.format(
              "Constraint Builder found Condition States and Event States simultaneously on %s",
              node));
    }
  }

  private static <T> void checkStatesExist(Collection<T> eventStateIDs, BayesianNetworkData data) {
    eventStateIDs.forEach(sid -> checkStateExists(sid, data));
  }

  private static <T> void checkStateExists(T stateID, BayesianNetworkData data) {
    if (data.getNodeStateIDsMap().containsKey(stateID)) return;
    throw new ParameterConstraintBuilderException(
        String.format("No event state %s in data", stateID.toString()));
  }

  public static <T> MarginalConstraint buildConstraint(
      T eventStateID, double probability, BayesianNetworkData data) {
    checkStatesExist(List.of(eventStateID), data);
    checkProbabilityIsValid(probability);
    return new MarginalConstraint(getNodeState(eventStateID, data), probability);
  }
}
