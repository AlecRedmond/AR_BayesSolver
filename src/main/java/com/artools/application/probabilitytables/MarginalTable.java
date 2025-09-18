package com.artools.application.probabilitytables;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public class MarginalTable extends ProbabilityTable {
  private final Node eventNode;

  public MarginalTable(
      String tableName, Map<Set<NodeState>, Double> probabilitiesMap, Node eventNode) {
    super(tableName, probabilitiesMap, Set.of(eventNode), Set.of(eventNode), new HashSet<>());
    this.eventNode = eventNode;
  }

  public void addProbability(NodeState state, double toAdd) {
    if (Double.isNaN(toAdd)) throw new IllegalArgumentException("Found a NaN");
    Set<NodeState> stateSet = Set.of(state);
    double oldVal = super.getProbability(stateSet);
    double newVal = oldVal + toAdd;
    super.setProbability(stateSet, newVal);
  }
}
