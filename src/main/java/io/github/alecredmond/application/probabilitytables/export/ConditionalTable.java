package io.github.alecredmond.application.probabilitytables.export;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.export.probabilityvector.ProbabilityVector;
import io.github.alecredmond.method.probabilitytables.TableUtils;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public class ConditionalTable extends ProbabilityTable {

  private final Node networkNode;

  public ConditionalTable(
      String tableID,
      ProbabilityVector vector,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions,
      Node networkNode,
      Map<Object, Node> nodeIDMap,
      Map<Object, NodeState> nodeStateIDMap) {
    super(nodeStateIDMap, nodeIDMap, vector, tableID, nodes, events, conditions);
    this.networkNode = networkNode;
  }

  @Override
  public void marginalizeTable() {
    TableUtils.marginalizeConditionalTable(this);
  }
}
