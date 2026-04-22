package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class ProbabilityMapper extends BaseVectorIterator implements VectorIterator {
  private final Map<Set<NodeState>, Double> probabilityMap = new LinkedHashMap<>();

  public ProbabilityMapper(VectorOdometer vectorOdometer) {
    super(vectorOdometer);
    performRun();
  }

  @Override
  public void performRun() {
    double[] p = vectorOdometer.getProbabilities();
    iterateInner(
        (odometer, index) -> probabilityMap.put(getStateSet(odometer.getStates()), p[index]));
  }

  private Set<NodeState> getStateSet(NodeState[] states) {
    return Arrays.stream(states).collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
