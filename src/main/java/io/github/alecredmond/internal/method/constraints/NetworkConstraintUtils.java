package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.util.*;
import java.util.stream.Collectors;

public class NetworkConstraintUtils {

  private NetworkConstraintUtils() {}

  public static void addConstraints(
      Collection<ProbabilityConstraint> constraints, BayesianNetworkData networkData) {
    constraints.forEach(c -> addConstraint(c, networkData));
  }

  public static void addConstraint(
      ProbabilityConstraint constraint, BayesianNetworkData networkData) {
    if (new ConstraintValidator(constraint, networkData).validate()) {
      networkData.getConstraints().add(constraint);
    }
  }

  public static void addConstraint(
      NodeState eventState, double probability, BayesianNetworkData networkData) {
    addConstraint(eventState, Set.of(), probability, networkData);
  }

  public static void addConstraint(
      NodeState eventState,
      Set<NodeState> conditionStates,
      double probability,
      BayesianNetworkData networkData) {
    ProbabilityConstraint probabilityConstraint =
        conditionStates.isEmpty()
            ? new MarginalConstraint(eventState, probability)
            : new ConditionalConstraint(eventState, conditionStates, probability);
    addConstraint(probabilityConstraint, networkData);
  }

  public static boolean removeAllConstraints(BayesianNetworkData networkData) {
    List<ProbabilityConstraint> probabilityConstraints = networkData.getConstraints();
    if (probabilityConstraints.isEmpty()) {
      return false;
    }
    networkData.setConstraints(new ArrayList<>());
    return true;
  }

  public static boolean removeConstraint(NodeState eventStateId, BayesianNetworkData networkData) {
    return removeConstraint(eventStateId, Set.of(), networkData);
  }

  public static boolean removeConstraint(
      NodeState eventState, Set<NodeState> conditionStates, BayesianNetworkData networkData) {
    ProbabilityConstraint probabilityConstraint =
        getConstraint(eventState, conditionStates, networkData);
    if (probabilityConstraint == null) {
      return false;
    }
    return removeConstraint(probabilityConstraint, networkData);
  }

  public static ProbabilityConstraint getConstraint(
      NodeState eventState, Set<NodeState> conditionStates, BayesianNetworkData networkData) {
    if (conditionStates.isEmpty()) {
      return getConstraint(eventState, networkData);
    }

    return networkData.getConstraints().parallelStream()
        .filter(ConditionalConstraint.class::isInstance)
        .map(ConditionalConstraint.class::cast)
        .filter(cc -> cc.getEventState().equals(eventState))
        .filter(cc -> cc.getConditionStates().equals(conditionStates))
        .findFirst()
        .orElse(null);
  }

  public static boolean removeConstraint(
      ProbabilityConstraint probabilityConstraint, BayesianNetworkData networkData) {
    return networkData.getConstraints().remove(probabilityConstraint);
  }

  public static MarginalConstraint getConstraint(
      NodeState eventState, BayesianNetworkData networkData) {
    return networkData.getConstraints().parallelStream()
        .filter(MarginalConstraint.class::isInstance)
        .map(MarginalConstraint.class::cast)
        .filter(mc -> mc.getEventState().equals(eventState))
        .findFirst()
        .orElse(null);
  }

  public static boolean removeAllConstraintsContaining(Node node, BayesianNetworkData data) {
    Set<ProbabilityConstraint> toRemove =
        data.getConstraints().parallelStream()
            .filter(constraint -> constraint.getAllNodes().contains(node))
            .collect(Collectors.toSet());
    return data.getConstraints().removeAll(toRemove);
  }

  public static boolean removeAllConstraintsContaining(NodeState state, BayesianNetworkData data) {
    Set<ProbabilityConstraint> toRemove =
        data.getConstraints().parallelStream()
            .filter(constraint -> constraint.getAllStates().contains(state))
            .collect(Collectors.toSet());
    return data.getConstraints().removeAll(toRemove);
  }
}
