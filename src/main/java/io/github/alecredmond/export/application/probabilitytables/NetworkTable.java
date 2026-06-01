package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class NetworkTable extends ProbabilityTable {
  protected <T extends Serializable> NetworkTable(
      Map<Serializable, NodeState> nodeStateIDMap,
      Map<Serializable, Node> nodeIDMap,
      ProbabilityVector vector,
      T tableName,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions) {
    super(nodeStateIDMap, nodeIDMap, vector, tableName, nodes, events, conditions);
  }

  public abstract Node getNetworkNode();
}
