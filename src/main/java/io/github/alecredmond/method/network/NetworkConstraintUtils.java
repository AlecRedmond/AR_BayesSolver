package io.github.alecredmond.method.network;

import io.github.alecredmond.application.constraints.ConditionalConstraint;
import io.github.alecredmond.application.constraints.Constraint;
import io.github.alecredmond.application.constraints.MarginalConstraint;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.method.constraints.ConstraintValidator;
import java.util.*;

class NetworkConstraintUtils {

  private NetworkConstraintUtils() {}

  public static void addConstraints(
      Collection<Constraint> constraints, BayesianNetworkData networkData) {
    constraints.forEach(c -> addConstraint(c, networkData));
  }

  public static void addConstraint(Constraint constraint, BayesianNetworkData networkData) {
    ConstraintValidator.validate(constraint, networkData);
    networkData.getConstraints().add(constraint);
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
    Constraint constraint;
    if (conditionStates.isEmpty()) {
      constraint = new MarginalConstraint(eventState, probability);
    } else {
      constraint = new ConditionalConstraint(eventState, conditionStates, probability);
    }
    addConstraint(constraint, networkData);
  }

  public static boolean removeAllConstraints(BayesianNetworkData networkData) {
    List<Constraint> constraints = networkData.getConstraints();
    if (constraints.isEmpty()) {
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
    Constraint constraint = getConstraint(eventState, conditionStates, networkData);
    if (constraint == null) {
      return false;
    }
    return removeConstraint(constraint, networkData);
  }

  public static Constraint getConstraint(
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

  public static boolean removeConstraint(Constraint constraint, BayesianNetworkData networkData) {
    return networkData.getConstraints().remove(constraint);
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
}
