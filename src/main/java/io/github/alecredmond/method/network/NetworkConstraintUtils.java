package io.github.alecredmond.method.network;

import io.github.alecredmond.application.constraints.ConditionalConstraint;
import io.github.alecredmond.application.constraints.MarginalConstraint;
import io.github.alecredmond.application.constraints.Constraint;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.method.constraints.ConstraintBuilder;
import io.github.alecredmond.method.node.NodeUtils;
import java.util.*;

public class NetworkConstraintUtils {

  private NetworkConstraintUtils() {}

  public static void addConstraints(
          Collection<Constraint> constraints, BayesianNetworkData networkData) {
    constraints.forEach(c -> addConstraint(c, networkData));
  }

  public static void addConstraint(
          Constraint constraint, BayesianNetworkData networkData) {
    checkParametersAreUnique(constraint, networkData);
    networkData.getConstraints().add(constraint);
  }

  private static void checkParametersAreUnique(
          Constraint constraint, BayesianNetworkData networkData) {
    Set<NodeState> eventStates = constraint.getEventStates();
    Set<NodeState> conditionStates = constraint.getConditionStates();
    boolean parametersAreUnique =
        networkData.getConstraints().parallelStream()
            .filter(c -> eventStates.equals(c.getEventStates()))
            .noneMatch(c -> conditionStates.equals(c.getConditionStates()));
    if (parametersAreUnique) {
      return;
    }
    throw new IllegalArgumentException(
        String.format(
            "Attempted to add a constraint C(%s | %s), which already exists!",
            NodeUtils.formatToString(eventStates), NodeUtils.formatToString(conditionStates)));
  }

  public static <T> void addConstraint(
      T eventStateID, double probability, BayesianNetworkData networkData) {
    addConstraint(eventStateID, List.of(), probability, networkData);
  }

  public static <T, E> void addConstraint(
      T eventStateID,
      Collection<E> conditionStateIDs,
      double probability,
      BayesianNetworkData networkData) {
    networkData
        .getConstraints()
        .add(
            ConstraintBuilder.buildConstraint(
                eventStateID, conditionStateIDs, probability, networkData));
  }

  public static boolean removeAllConstraints(BayesianNetworkData networkData) {
    List<Constraint> constraints = networkData.getConstraints();
    if (constraints.isEmpty()) {
      return false;
    }
    networkData.setConstraints(new ArrayList<>());
    return true;
  }

  public static <T> boolean removeConstraint(T eventStateId, BayesianNetworkData networkData) {
    return removeConstraint(eventStateId, List.of(), networkData);
  }

  public static <T, E> boolean removeConstraint(
      T eventStateId, Collection<E> conditionStateIds, BayesianNetworkData networkData) {
    Constraint constraint = getConstraint(eventStateId, conditionStateIds, networkData);
    if (constraint == null) {
      return false;
    }
    return removeConstraint(constraint, networkData);
  }

  public static <T, E> Constraint getConstraint(
      T eventStateId, Collection<E> conditionStateIds, BayesianNetworkData networkData) {
    if (conditionStateIds.isEmpty()) {
      return getConstraint(eventStateId, networkData);
    }

    return networkData.getConstraints().parallelStream()
        .filter(ConditionalConstraint.class::isInstance)
        .map(ConditionalConstraint.class::cast)
        .filter(cc -> cc.getEventState().getId().equals(eventStateId))
        .filter(cc -> allStateIdsMatch(cc.getConditionStates(), conditionStateIds))
        .findFirst()
        .orElse(null);
  }

  public static boolean removeConstraint(
          Constraint constraint, BayesianNetworkData networkData) {
    return networkData.getConstraints().remove(constraint);
  }

  public static <T> MarginalConstraint getConstraint(
      T eventStateId, BayesianNetworkData networkData) {
    return networkData.getConstraints().parallelStream()
        .filter(MarginalConstraint.class::isInstance)
        .map(MarginalConstraint.class::cast)
        .filter(mc -> mc.getEventState().getId().equals(eventStateId))
        .findFirst()
        .orElse(null);
  }

  public static <T> boolean allStateIdsMatch(Set<NodeState> states, Collection<T> stateIds) {
    Set<Object> idSet = new HashSet<>(stateIds);
    return states.stream().map(NodeState::getId).allMatch(idSet::contains);
  }
}
