package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.*;
import java.util.function.Predicate;
import lombok.NonNull;

public class NetworkConstraintUtils {

  private NetworkConstraintUtils() {}

  public static List<ConstraintValidationException> addConstraints(
      Collection<ProbabilityConstraint> constraints, BayesianNetworkData networkData) {
    return constraints.stream()
        .map(c -> addConstraint(c, networkData))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  public static Optional<ConstraintValidationException> addConstraint(
      ProbabilityConstraint constraint, BayesianNetworkData networkData) {
    return addConstraintFromBuilder(new ConstraintBuilder(constraint, networkData), networkData);
  }

  private static Optional<ConstraintValidationException> addConstraintFromBuilder(
      ConstraintBuilder builder, BayesianNetworkData networkData) {
    Optional<ConstraintValidationException> e = builder.getException();
    if (e.isPresent()) {
      return e;
    }
    networkData.getConstraints().add(builder.getConstraint());
    return Optional.empty();
  }

  public static Optional<ConstraintValidationException> addConstraints(
      Set<NodeState> eventStates,
      Set<NodeState> conditionStates,
      double probability,
      BayesianNetworkData networkData) {
    List<Set<NodeState>> splitConditions = NodeUtils.splitStatesSharingNodes(conditionStates);
    List<Optional<ConstraintValidationException>> list = new ArrayList<>();
    for (Set<NodeState> condition : splitConditions) {
      list.add(addConstraint(eventStates, condition, probability, networkData));
    }
    return list.stream().findFirst().orElse(Optional.empty());
  }

  public static Optional<ConstraintValidationException> addConstraint(
      Set<NodeState> eventStates,
      Set<NodeState> conditionStates,
      double probability,
      BayesianNetworkData networkData) {
    return addConstraintFromBuilder(
        new ConstraintBuilder(eventStates, conditionStates, probability, networkData), networkData);
  }

  public static boolean removeAllConstraints(BayesianNetworkData networkData) {
    List<ProbabilityConstraint> probabilityConstraints = networkData.getConstraints();
    if (probabilityConstraints.isEmpty()) {
      return false;
    }
    networkData.setConstraints(new ArrayList<>());
    return true;
  }

  public static boolean removeConstraint(
      Set<NodeState> eventStates, Set<NodeState> conditionStates, BayesianNetworkData networkData) {
    return removeConstraints(exactMatch(eventStates, conditionStates), networkData);
  }

  public static boolean removeConstraints(
      Predicate<ProbabilityConstraint> predicate, BayesianNetworkData data) {
    return data.getConstraints().removeIf(predicate);
  }

  private static Predicate<ProbabilityConstraint> exactMatch(
      Set<NodeState> eventStates, Set<NodeState> conditionStates) {
    return constraint ->
        constraint.getEventStates().equals(eventStates)
            && constraint.getConditionStates().equals(conditionStates);
  }

  public static boolean removeConstraint(
      @NonNull ProbabilityConstraint probabilityConstraint, BayesianNetworkData networkData) {
    return networkData.getConstraints().remove(probabilityConstraint);
  }

  public static ProbabilityConstraint getConstraint(
      Set<NodeState> eventStates, Set<NodeState> conditionStates, BayesianNetworkData networkData) {
    return networkData.getConstraints().parallelStream()
        .filter(exactMatch(eventStates, conditionStates))
        .findFirst()
        .orElse(null);
  }
}
