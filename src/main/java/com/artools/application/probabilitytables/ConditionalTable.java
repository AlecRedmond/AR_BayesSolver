package com.artools.application.probabilitytables;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public class ConditionalTable extends ProbabilityTable {
  private final Node eventNode;

  public ConditionalTable(
      String tableName,
      Map<Set<NodeState>, Integer> indexMap,
      double[] probabilities,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions,
      Node eventNode) {
    super(indexMap, probabilities, tableName, nodes, events, conditions);
    this.eventNode = eventNode;
  }
}
