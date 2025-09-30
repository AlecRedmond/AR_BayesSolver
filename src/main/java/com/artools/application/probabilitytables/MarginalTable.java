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
      Map<Set<NodeState>, Integer> indexMap,
      double[] probabilities,
      String tableName,
      Node eventNode) {
    super(
        indexMap, probabilities, tableName, Set.of(eventNode), Set.of(eventNode), new HashSet<>());
    this.eventNode = eventNode;
  }
}
