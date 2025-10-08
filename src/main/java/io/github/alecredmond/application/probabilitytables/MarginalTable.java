package io.github.alecredmond.application.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

/**
 * Represents the unconditional probability distribution for a single Node in the Bayesian Network.
 * Used in Nodes without parents or in network observations
 */
@Getter
public class MarginalTable extends ProbabilityTable {
  /** The node in the network associated with the table */
  private final Node networkNode;

  /**
   * @param nodeStateIDMap a map which can obtain a NodeState from its ID
   * @param nodeIDMap a map which can obtain a Node from its ID
   * @param indexMap a map that links every set of Node States to its associated probability on the
   *     array
   * @param tableID The Identifier for the table
   * @param probabilities a flat array of probability values
   * @param networkNode the single node measured by the table
   */
  public MarginalTable(
      Map<Set<NodeState>, Integer> indexMap,
      double[] probabilities,
      String tableID,
      Node networkNode,
      Map<Object, NodeState> nodeStateIDMap,
      Map<Object, Node> nodeIDMap) {
    super(
        nodeStateIDMap,
        nodeIDMap,
        indexMap,
        probabilities,
        tableID,
        Set.of(networkNode),
        Set.of(networkNode),
        new HashSet<>());
    this.networkNode = networkNode;
  }
}
