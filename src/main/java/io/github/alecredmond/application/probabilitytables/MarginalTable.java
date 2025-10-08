package io.github.alecredmond.application.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public class MarginalTable extends ProbabilityTable {
  private final Node eventNode;

  /**
   * @param nodeStateIDMap a map which can obtain a NodeState from its ID
   * @param nodeIDMap a map which can obtain a Node from its ID
   * @param indexMap a map that links every set of Node States to its associated probability on the
   *     array
   * @param tableID The Identifier for the table
   * @param probabilities a flat array of probability values
   * @param eventNode the single node measured by the table
   */
  public MarginalTable(
      Map<Set<NodeState>, Integer> indexMap,
      double[] probabilities,
      String tableID,
      Node eventNode,
      Map<Object, NodeState> nodeStateIDMap,
      Map<Object, Node> nodeIDMap) {
    super(
        nodeStateIDMap,
        nodeIDMap,
        indexMap,
        probabilities,
        tableID,
        Set.of(eventNode),
        Set.of(eventNode),
        new HashSet<>());
    this.eventNode = eventNode;
  }
}
