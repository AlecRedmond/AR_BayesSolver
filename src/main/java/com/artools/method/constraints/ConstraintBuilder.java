package com.artools.method.constraints;

import com.artools.application.constraints.ConditionalConstraint;
import com.artools.application.constraints.MarginalConstraint;
import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.network.BayesNetData;
import com.artools.application.node.NodeState;
import java.util.Collection;
import java.util.List;

public class ConstraintBuilder {
  //Test to see if commit will work on new machine

  private ConstraintBuilder() {}

  public static <T, E> ParameterConstraint buildConstraint(
      Collection<T> eventStateIDs,
      Collection<E> conditionStateIDs,
      double probability,
      BayesNetData data) {
    if (eventStateIDs.size() == 1) {
      return buildConstraint(
          eventStateIDs.stream().findAny().orElseThrow(), conditionStateIDs, probability, data);
    }
    List<NodeState> eventStates = getNodeStates(eventStateIDs, data);
    List<NodeState> conditionStates = getNodeStates(conditionStateIDs, data);
    return new ParameterConstraint(eventStates, conditionStates, probability);
  }

  public static <T, E> ParameterConstraint buildConstraint(
      T eventStateID, Collection<E> conditionStateIDs, double probability, BayesNetData data) {
    NodeState eventState = getNodeState(eventStateID, data);
    List<NodeState> conditionStates = getNodeStates(conditionStateIDs, data);
    if (conditionStates.isEmpty()) return new MarginalConstraint(eventState, probability);
    return new ConditionalConstraint(eventState, conditionStates, probability);
  }

  private static <E> List<NodeState> getNodeStates(
      Collection<E> conditionStateIDs, BayesNetData data) {
    return conditionStateIDs.stream().map(sID -> data.getNodeStateMap().get(sID)).toList();
  }

  private static <T> NodeState getNodeState(T eventStateID, BayesNetData data) {
    return data.getNodeStateMap().get(eventStateID);
  }

  public static <T> MarginalConstraint buildConstraint(
      T eventStateID, double probability, BayesNetData data) {
    return new MarginalConstraint(getNodeState(eventStateID, data), probability);
  }
}
