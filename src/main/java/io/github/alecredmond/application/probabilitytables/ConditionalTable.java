package io.github.alecredmond.application.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.method.probabilitytables.ProbabilityVectorUtils;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public class ConditionalTable extends ProbabilityTable {

  private final Node networkNode;

  public ConditionalTable(
      String tableID,
      ProbabilityVector vector,
      ProbabilityVectorUtils utils,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions,
      Node networkNode,
      Map<Object, Node> nodeIDMap,
      Map<Object, NodeState> nodeStateIDMap) {
    super(nodeStateIDMap, nodeIDMap, vector, utils, tableID, nodes, events, conditions);
    this.networkNode = networkNode;
  }
}
