package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.method.probabilitytables.ConditionalTableHelper;
import io.github.alecredmond.internal.application.probabilitytables.base.SingleEventTable;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilderData;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ConditionalTableImpl extends SingleEventTable<ConditionalTable, ConditionalTableHelper>
    implements ConditionalTable {
  public ConditionalTableImpl(TableBuilderData tableBuilderData) {
    super(tableBuilderData);
  }

  public ConditionalTableImpl(
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
