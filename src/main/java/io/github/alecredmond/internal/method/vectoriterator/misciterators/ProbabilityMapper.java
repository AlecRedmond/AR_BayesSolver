package io.github.alecredmond.internal.method.vectoriterator.misciterators;

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
public class ProbabilityMapper implements BaseOdometerResetLogic, StateUpdater {
  private final Map<Set<NodeState>, Double> probabilityMap = new LinkedHashMap<>();
  private final VectorIterator iterator;
  private final VectorOdometer odometer;

  public ProbabilityMapper(ProbabilityTable table) {
    this.odometer = new VectorOdometer(table.getVector());
    this.iterator = new VectorIterator(odometer, this);
    performRun();
  }

  public void performRun() {
    double[] p = odometer.getProbabilities();
    NodeState[] states = odometer.getStates();
    iterator.iterateInner((o, i) -> probabilityMap.put(getStateSet(states), p[i]));
  }

  private Set<NodeState> getStateSet(NodeState[] states) {
    return Arrays.stream(states).collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return n -> true;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return n -> false;
  }
}
