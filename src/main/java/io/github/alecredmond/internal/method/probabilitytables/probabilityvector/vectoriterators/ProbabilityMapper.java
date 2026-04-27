package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import static io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator.UPDATE_STATES;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerResetLogic;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class ProbabilityMapper implements OdometerResetLogic {
  private final Map<Set<NodeState>, Double> probabilityMap = new LinkedHashMap<>();
  private final VectorIterator iterator;

  public ProbabilityMapper(ProbabilityTable table) {
    this.iterator = new VectorIterator(table.getVector(), this, UPDATE_STATES);
    performRun();
  }

  public void performRun() {
    VectorOdometer odometer = iterator.getVectorOdometer();
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
