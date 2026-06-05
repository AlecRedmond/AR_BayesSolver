package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.RootNodeTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.method.probabilitytables.RootNodeTableHelper;
import io.github.alecredmond.internal.application.probabilitytables.base.SingleEventTable;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilderData;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RootNodeTableImpl extends SingleEventTable<RootNodeTable, RootNodeTableHelper>
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
