package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import static io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.BaseVectorIterator.*;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerResetLogic;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class ConstraintBuilderIterator implements OdometerResetLogic {
  private final BaseVectorIterator iterator;
  private final ProbabilityTable table;
  private List<ProbabilityConstraint> built;

  public ConstraintBuilderIterator(ProbabilityTable table) {
    this.iterator = new BaseVectorIterator(table.getVector(), this, UPDATE_STATES);
    this.table = table;
    performRun();
  }

  public void performRun() {
    built = new ArrayList<>();
    double[] p = iterator.getVectorOdometer().getProbabilities();
    NodeState[] states = iterator.getVectorOdometer().getStates();
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

  public void reset(){
      iterator.reset();
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
