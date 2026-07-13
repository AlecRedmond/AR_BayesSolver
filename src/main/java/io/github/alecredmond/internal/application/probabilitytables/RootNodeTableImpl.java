package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.RootNodeTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.export.probabilitytables.RootNodeTableQueryTool;
import io.github.alecredmond.internal.application.probabilitytables.base.SingleEventTable;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilderData;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RootNodeTableImpl extends SingleEventTable<RootNodeTableQueryTool>
    implements RootNodeTable {
  public RootNodeTableImpl(TableBuilderData tableBuilderData) {
    super(tableBuilderData);
  }

  public RootNodeTableImpl(
      Map<Serializable, NodeState> nodeStateIDMap,
      Map<Serializable, Node> nodeIDMap,
      ProbabilityVector vector,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions,
      Node eventNode) {
    super(nodeStateIDMap, nodeIDMap, vector, nodes, events, conditions, eventNode);
  }

  @Override
  public Node getNetworkNode() {
    return eventNode;
  }
}
