package io.github.alecredmond.internal.application.probabilitytables.base;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityVector;
import io.github.alecredmond.export.method.probabilitytables.TableQueryTool;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilderData;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class SingleEventTable<R extends TableQueryTool>
    extends ProbabilityTableBase<R> {
  protected final Node eventNode;

  protected SingleEventTable(TableBuilderData tableBuilderData) {
    super(tableBuilderData);
    this.eventNode = tableBuilderData.getEventNode();
  }

  protected SingleEventTable(
      Map<Serializable, NodeState> nodeStateIDMap,
      Map<Serializable, Node> nodeIDMap,
      ProbabilityVector vector,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions,
      Node eventNode) {
    super(nodeStateIDMap, nodeIDMap, vector, nodes, events, conditions);
    this.eventNode = eventNode;
  }
}
