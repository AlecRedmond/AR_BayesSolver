package io.github.alecredmond.application.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public class ConditionalTable extends ProbabilityTable {

  private final Node networkNode;

  public ConditionalTable(
      String tableID,
      Map<Set<NodeState>, Integer> indexMap,
      double[] probabilities,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions,
      Node networkNode,
      Map<Object, Node> nodeIDMap,
      Map<Object, NodeState> nodeStateIDMap) {
    super(nodeStateIDMap, nodeIDMap, indexMap, probabilities, tableID, nodes, events, conditions);
    this.networkNode = networkNode;
  }
}
