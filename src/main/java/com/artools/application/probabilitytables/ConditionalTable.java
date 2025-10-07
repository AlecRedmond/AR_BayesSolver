package com.artools.application.probabilitytables;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public class ConditionalTable extends ProbabilityTable {
  private final Node eventNode;

    /**
     * @param nodeStateIDMap a map which can obtain a NodeState from its ID
     * @param nodeIDMap a map which can obtain a Node from its ID
     * @param indexMap a map that links every set of Node States to its associated probability on the
     *     array
     * @param tableID The Identifier for the table
     * @param probabilities a flat array of probability values
     * @param nodes all nodes associated with the table
     * @param events Event Nodes associated with the table, P(Events|Conditions)
     * @param conditions Condition Nodes associated with the table, P(Events|Conditions)
     */
  public ConditionalTable(
      String tableID,
      Map<Set<NodeState>, Integer> indexMap,
      double[] probabilities,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions,
      Node eventNode,
      Map<?, Node> nodeIDMap,
      Map<?, NodeState> nodeStateIDMap) {
    super(nodeStateIDMap, nodeIDMap, indexMap, probabilities, tableID, nodes, events, conditions);
    this.eventNode = eventNode;
  }
}
