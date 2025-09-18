package com.artools.application.probabilitytables;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public class JunctionTreeTable extends ProbabilityTable {
  private final Map<Set<NodeState>, Double> observedProbMap;
  private final Set<NodeState> observedStates;
  private boolean observed;

  public <T> JunctionTreeTable(
      T tableID, Map<Set<NodeState>, Double> probabilityMap, Set<Node> events) {
    super(tableID, probabilityMap, events, events, new HashSet<>());
    observedProbMap = new HashMap<>();
    observedStates = new HashSet<>();
  }

  public void setObserved(Set<NodeState> newEvidence, boolean observed) {
    this.observed = observed;
    this.observedStates.clear();
    this.observedStates.addAll(newEvidence);
  }

  public void setByRatio(Set<NodeState> request, double ratio) {
    double newVal = getCorrectProb(request) * ratio;
    setCorrectProb(request, newVal);
  }

  public double getCorrectProb(Set<NodeState> request) {
    if (!observed) return getProbability(request);
    return getObservedProb(request);
  }

  public void setCorrectProb(Set<NodeState> request, double newVal) {
    if (!observed) setProbability(request, newVal);
    else setObservedProb(request, newVal);
  }

  public double getObservedProb(Set<NodeState> request) {
    return observedProbMap.get(request);
  }

  public void setObservedProb(Set<NodeState> request, double newVal) {
    observedProbMap.put(request, newVal);
  }
}
