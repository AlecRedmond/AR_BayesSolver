package io.github.alecredmond.application.probabilitytables.export;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.export.probabilityvector.ProbabilityVector;
import io.github.alecredmond.method.probabilitytables.TableUtils;

import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class ProbabilityTable {
  protected final Map<Object, NodeState> nodeStateIDMap;
  protected final Map<Object, Node> nodeIDMap;
  protected final ProbabilityVector vector;
  protected final Set<Node> nodes;
  protected final Set<Node> events;
  protected final Set<Node> conditions;
  @Setter protected Object tableID;

  protected <T> ProbabilityTable(
      Map<Object, NodeState> nodeStateIDMap,
      Map<Object, Node> nodeIDMap,
      ProbabilityVector vector,
      T tableID,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions) {
    this.nodeStateIDMap = nodeStateIDMap;
    this.nodeIDMap = nodeIDMap;
    this.vector = vector;
    this.tableID = tableID;
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
