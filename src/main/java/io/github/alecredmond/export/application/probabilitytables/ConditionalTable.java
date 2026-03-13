package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
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
