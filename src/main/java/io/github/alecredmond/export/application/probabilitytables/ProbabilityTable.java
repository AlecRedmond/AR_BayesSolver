package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class ProbabilityTable {
  @EqualsAndHashCode.Include protected final UUID uuid;
  protected final Map<Object, NodeState> nodeStateIDMap;
  protected final Map<Object, Node> nodeIDMap;
  protected final ProbabilityVector vector;
  protected final Set<Node> nodes;
  protected final Set<Node> events;
  protected final Set<Node> conditions;
  @Setter protected Object tableName;

  protected <T> ProbabilityTable(
      Map<Object, NodeState> nodeStateIDMap,
      Map<Object, Node> nodeIDMap,
      ProbabilityVector vector,
      T tableName,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions) {
    this.uuid = UUID.randomUUID();
    this.nodeStateIDMap = nodeStateIDMap;
    this.nodeIDMap = nodeIDMap;
    this.vector = vector;
    this.tableName = tableName;
    this.nodes = nodes;
    this.events = events;
    this.conditions = conditions;
  }

  public <T> double getProbabilityFromIDs(Collection<T> stateIDs) {
    return getProbability(stateIDs.stream().map(nodeStateIDMap::get).toList());
  }

  public double getProbability(Collection<NodeState> request) {
    TableUtils.confirmAllNodesQueried(request, this);
    return TableUtils.getProbability(request, this);
  }

  public abstract void marginalizeTable();
}
