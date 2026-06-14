package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.utils.DoublePrecision;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.BaseOdometerResetLogic;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.updatelogictypes.StateUpdater;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class ConstraintBuilderIterator implements BaseOdometerResetLogic, StateUpdater {
  private final VectorIterator<VectorOdometer> iterator;
  private final Set<Node> events;
  private final Set<Node> conditions;
  private final VectorOdometer odometer;
  private List<ProbabilityConstraint> built;

  public ConstraintBuilderIterator(ProbabilityTable table) {
    this.events = table.getEvents();
    this.conditions = table.getConditions();
    this.odometer = new VectorOdometer(table.getVector());
    this.iterator = new VectorIterator<>(odometer, this);
    performRun();
  }

  public void performRun() {
    built = new ArrayList<>();
    double[] p = odometer.getProbabilities();
    NodeState[] states = odometer.getStates();
    Runnable function =
        conditions.isEmpty() ? createMarginals(p, states) : createConditionals(p, states);
    iterator.iterateOuter(function);
  }

  private Runnable createMarginals(double[] p, NodeState[] states) {
    return () -> {
      List<MarginalConstraint> constraints = new ArrayList<>();
      int[] count = {0};
      iterator.iterateInner(
          (o, i) -> {
            count[0]++;
            double prob = p[i];
            if (prob > 1.0 || prob < 0.0) return;
            NodeState event = states[0];
            constraints.add(new MarginalConstraint(event, prob));
          });
      validateConstraintSums(constraints, count[0]);
      built.addAll(constraints);
    };
  }

  private Runnable createConditionals(double[] p, NodeState[] states) {
    return () -> {
      List<ConditionalConstraint> constraints = new ArrayList<>();
      Set<NodeState> conds = getStatesFromSet(states, conditions);
      int[] count = {0};
      iterator.iterateInner(
          (o, i) -> {
            count[0]++;
            double prob = p[i];
            if (prob > 1.0 || prob < 0.0) return;
            NodeState event = getStatesFromSet(states, events).iterator().next();
            constraints.add(new ConditionalConstraint(event, conds, prob));
          });
      validateConstraintSums(constraints, count[0]);
      built.addAll(constraints);
    };
  }

  private void validateConstraintSums(
      List<? extends ProbabilityConstraint> constraints, int count) {
    if (constraints.isEmpty()) return;
    Set<NodeState> condition = constraints.getFirst().getConditionStates();
    double sum = constraints.stream().mapToDouble(ProbabilityConstraint::getProbability).sum();
    if (constraints.size() < count && sum >= 1.0) {
      throw new ConstraintValidationException(
          "Constraints on [%s] the condition [%s] would sum to > 1"
              .formatted(
                  events.iterator().next().getId(), NodeUtils.formatStatesToString(condition)));
    }
    if (constraints.size() != count) return;
    if (DoublePrecision.fuzzyEquals(sum, 1.0)) {
      // IPFP will automatically solve the last state if all others are known.
      constraints.removeLast();
      return;
    }
    throw new ConstraintValidationException(
        "Constraints on [%s] the condition [%s] do not sum to 1"
            .formatted(
                events.iterator().next().getId(), NodeUtils.formatStatesToString(condition)));
  }

  private Set<NodeState> getStatesFromSet(NodeState[] states, Set<Node> set) {
    return Arrays.stream(states)
        .filter(state -> set.contains(state.getNode()))
        .collect(Collectors.toSet());
  }

  public ConstraintBuilderIterator(
      Set<Node> events, Set<Node> conditions, ProbabilityVector vector) {
    this.events = events;
    this.conditions = conditions;
    this.odometer = new VectorOdometer(vector);
    this.iterator = new VectorIterator<>(odometer, this);
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return events::contains;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return conditions::contains;
  }
}
