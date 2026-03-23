package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import java.io.Serializable;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode()
public abstract class ProbabilityTable {
  protected final Map<Serializable, NodeState> nodeStateIDMap;
  protected final Map<Serializable, Node> nodeIDMap;
  protected final ProbabilityVector vector;
  protected final Set<Node> nodes;
  protected final Set<Node> events;
  protected final Set<Node> conditions;
  @Exclude @Setter protected Serializable tableName;

  protected <T extends Serializable> ProbabilityTable(
      Map<Serializable, NodeState> nodeStateIDMap,
      Map<Serializable, Node> nodeIDMap,
      ProbabilityVector vector,
      T tableName,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions) {
    this.nodeStateIDMap = nodeStateIDMap;
    this.nodeIDMap = nodeIDMap;
    this.vector = vector;
    this.tableName = tableName;
    this.nodes = nodes;
    this.events = events;
    this.conditions = conditions;
  }

  public <T extends Serializable> double getProbabilityFromIDs(Collection<T> stateIDs) {
    return getProbability(stateIDs.stream().map(nodeStateIDMap::get).toList());
  }

  public double getProbability(Collection<NodeState> request) {
    TableUtils.confirmAllNodesQueried(request, this);
    return TableUtils.getProbability(request, this);
  }

  public abstract void marginalizeTable();
}
