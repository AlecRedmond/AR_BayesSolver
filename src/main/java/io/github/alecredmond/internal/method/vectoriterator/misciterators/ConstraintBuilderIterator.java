package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
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
  private final ProbabilityTable table;
  private final VectorOdometer odometer;
  private List<ProbabilityConstraint> built;

  public ConstraintBuilderIterator(ProbabilityTable table) {
    this.odometer = new VectorOdometer(table.getVector());
    this.iterator = new VectorIterator<>(odometer, this);
    this.table = table;
    performRun();
  }

  public void performRun() {
    built = new ArrayList<>();
    double[] p = odometer.getProbabilities();
    NodeState[] states = odometer.getStates();
    int condsLength = states.length - 1;
    Runnable function =
        condsLength == 0 ? createConditionals(condsLength, p, states) : createMarginals(p, states);
    iterator.iterateOuter(function);
  }

  private Runnable createConditionals(int condsLength, double[] p, NodeState[] states) {
    return () -> {
      Set<NodeState> conds = getConds(states, condsLength);
      iterator.iterateInner(
          (o, i) -> {
            double prob = p[i];
            if (prob == 1.0) return; // As tables are initialized to 1.0
            NodeState event = states[condsLength];
            built.add(new ConditionalConstraint(event, conds, prob));
          });
    };
  }

  private Runnable createMarginals(double[] p, NodeState[] states) {
    return () ->
        iterator.iterateInner(
            (o, i) -> {
              double prob = p[i];
              if (prob == 1.0) return;
              NodeState event = states[0];
              built.add(new MarginalConstraint(event, prob));
            });
  }

  private Set<NodeState> getConds(NodeState[] states, int condsLength) {
    return Arrays.stream(states, 0, condsLength).collect(Collectors.toSet());
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    Set<Node> events = table.getEvents();
    return events::contains;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    Set<Node> conditions = table.getConditions();
    return conditions::contains;
  }
}
