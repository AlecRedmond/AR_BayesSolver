package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.method.probabilitytables.ConditionalTableHelper;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.ConditionalTableHelperImpl;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * A table mapping the distribution of probabilities over all states of a {@link Node}, conditional
 * on its parent states. This is used as the CPT for non-root nodes in a Bayesian Network. The table
 * contains a helper object for querying the probability distribution.
 *
 * @see ProbabilityTable
 */
@Slf4j
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ConditionalTable extends NetworkTable {
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
  protected ConditionalTableHelper buildHelper() {
    return new ConditionalTableHelperImpl(this);
  }

  @Override
  public ConditionalTableHelper getHelper() {
    return (ConditionalTableHelperImpl) helper;
  }
}
