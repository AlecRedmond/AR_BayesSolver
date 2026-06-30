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
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public abstract class ProbabilityTableBase<R extends TableQueryTool> {
  protected final Map<Serializable, NodeState> nodeStateIDMap;
  protected final Map<Serializable, Node> nodeIDMap;
  protected final ProbabilityVector vector;
  protected final Set<Node> nodes;
  protected final Set<Node> events;
  protected final Set<Node> conditions;
  @EqualsAndHashCode.Exclude protected R queryTool;
  @EqualsAndHashCode.Exclude protected Serializable tableName;

  protected ProbabilityTableBase(
      Map<Serializable, NodeState> nodeStateIDMap,
      Map<Serializable, Node> nodeIDMap,
      ProbabilityVector vector,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions) {
    this.nodeStateIDMap = nodeStateIDMap;
    this.nodeIDMap = nodeIDMap;
    this.vector = vector;
    this.nodes = nodes;
    this.events = events;
    this.conditions = conditions;
  }

  protected ProbabilityTableBase(TableBuilderData tableBuilderData) {
    this.nodeStateIDMap = tableBuilderData.getNodeStateIDMap();
    this.nodeIDMap = tableBuilderData.getNodeIDMap();
    this.vector = tableBuilderData.getVector();
    this.nodes = tableBuilderData.getNodes();
    this.events = tableBuilderData.getEvents();
    this.conditions = tableBuilderData.getConditions();
    this.tableName = tableBuilderData.getTableName();
  }

  public double[] getProbabilities() {
    return vector.getProbabilities();
  }
}
