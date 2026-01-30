package io.github.alecredmond.application.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
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
  protected final TableUtils utils;
  @Setter protected Object tableID;
  protected Set<Set<NodeState>> keySet;

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
    this.utils = new TableUtils(this);
    this.keySet = utils.generateStateCombinations(new ArrayList<>());
  }

  public <T> NodeState getNodeState(T nodeStateID) {
    return nodeStateIDMap.get(nodeStateID);
  }

  public <T> Node getNode(T nodeID) {
    return nodeIDMap.get(nodeID); // TODO - Hit Branch In Test Suite
  }

  public <T> double getProbabilityFromIDs(Collection<T> stateIDs) {
    return getProbability(stateIDs.stream().map(nodeStateIDMap::get).toList());
  }

  public double getProbability(Collection<NodeState> request) {
    utils.confirmAllNodesQueried(request);
    return utils.sumProbabilities(request);
  }

  public void setProbability(Set<NodeState> request, double probability) {
    utils.confirmAllNodesQueried(request);
    utils.setProbability(request,probability);
  }

  public int getIndex(Set<NodeState> request) {
    utils.confirmAllNodesQueried(request);
    return utils.collectIndexesWithStates(request).getFirst();
  }
}
