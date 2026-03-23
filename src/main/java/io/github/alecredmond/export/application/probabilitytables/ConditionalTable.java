package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.TableCopier;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ConditionalTable extends ProbabilityTable {
  private final Node networkNode;

  public ConditionalTable(
      Serializable tableName,
      ProbabilityVector vector,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions,
      Node networkNode,
      Map<Serializable, Node> nodeIDMap,
      Map<Serializable, NodeState> nodeStateIDMap) {
    super(nodeStateIDMap, nodeIDMap, vector, tableName, nodes, events, conditions);
    this.networkNode = networkNode;
  }

  @Override
  public void marginalizeTable() {
    TableUtils.marginalizeConditionalTable(this);
  }

  @Override
  public ConditionalTable copyTable() {
    return new TableCopier().copyConditional(this);
  }

  public <T extends Serializable> Map<NodeState, Double> getEventProbsWithConditionById(
      Collection<T> conditionStateIds) {
    return getEventProbsWithCondition(TableUtils.convertIDsToStates(conditionStateIds, this));
  }

  public Map<NodeState, Double> getEventProbsWithCondition(Collection<NodeState> conditionStates) {
    Map<NodeState, Double> map = TableUtils.getMapForConditions(conditionStates, this);
    if (map.isEmpty()) {
      log.error(
          "Error in getEventProbsWithCondition - request {} was not an exact match for all conditions in table {}",
          NodeUtils.formatStatesToString(conditionStates),
          this);
    }
    return map;
  }
}
