package io.github.alecredmond.application.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public class MarginalTable extends ProbabilityTable {

  private final Node networkNode;

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

  public <T> double getProbability(T nodeStateID) {
    return super.getProbability(Set.of(nodeStateID));
  }
}
