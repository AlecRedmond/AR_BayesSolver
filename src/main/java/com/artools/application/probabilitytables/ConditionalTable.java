package com.artools.application.probabilitytables;

import com.artools.application.node.Node;

import java.util.Map;
import java.util.Set;

import com.artools.application.node.NodeState;
import lombok.Getter;

@Getter
public class ConditionalTable extends ProbabilityTable {
  private final Node eventNode;

  public ConditionalTable(
      String tableName,
      Map<Set<NodeState>,Double> probabilityMap,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions,
      Node eventNode) {
    super(tableName,probabilityMap, nodes,events,conditions);
    this.eventNode = eventNode;
  }
}
