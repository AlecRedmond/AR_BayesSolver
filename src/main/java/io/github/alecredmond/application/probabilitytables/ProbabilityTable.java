package io.github.alecredmond.application.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.method.node.NodeUtils;
import io.github.alecredmond.method.probabilitytables.ProbabilityVectorUtils;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class ProbabilityTable {
  protected final Map<Object, NodeState> nodeStateIDMap;
  protected final Map<Object, Node> nodeIDMap;
  protected final ProbabilityVector vector;
  protected final ProbabilityVectorUtils utils;
  protected final Set<Node> nodes;
  protected final Set<Node> events;
  protected final Set<Node> conditions;
  @Setter protected Object tableID;
  protected Set<Set<NodeState>> keySet;

  protected <T> ProbabilityTable(
      Map<Object, NodeState> nodeStateIDMap,
      Map<Object, Node> nodeIDMap,
      ProbabilityVector vector,
      ProbabilityVectorUtils utils,
      T tableID,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions) {
    this.nodeStateIDMap = nodeStateIDMap;
    this.nodeIDMap = nodeIDMap;
    this.vector = vector;
    this.utils = utils;
    this.tableID = tableID;
    this.nodes = nodes;
    this.events = events;
    this.conditions = conditions;
    this.keySet = utils.generateStateCombinations(new HashMap<>());
  }

  public <T> NodeState getNodeState(T nodeStateID) {
    return nodeStateIDMap.get(nodeStateID);
  }

  public <T> Node getNode(T nodeID) {
    return nodeIDMap.get(nodeID); // TODO - Hit Branch In Test Suite
  }

  public <T> double getProbability(Collection<T> stateIDs) {
    return getProbability(getStates(stateIDs));
  }

  public double getProbability(Set<NodeState> key) {
    Map<Node, NodeState> request = getRequestMap(key);

    double probability = utils.sumProbabilitiesWithStates(request);

    if (Double.isNaN(probability)) {
      throw new IllegalArgumentException("map returned NaN");
    }

    return probability;
  }

  private <T> Set<NodeState> getStates(Collection<T> stateIDs) {
    Set<NodeState> set = new HashSet<>();
    for (Object stateID : stateIDs) {
      if (stateID instanceof NodeState state) {
        set.add(state);
        continue;
      }
      NodeState state = nodeStateIDMap.get(stateID);
      set.add(state);
    }
    return set;
  }

  private Map<Node, NodeState> getRequestMap(Set<NodeState> key) {
    Map<Node, NodeState> request = NodeUtils.generateRequest(key);

    if (!request.keySet().equals(nodes)) {
      throw new IllegalArgumentException(
          String.format("the Request to table %s did not match the keyset", tableID));
    }
    return request;
  }

  public void setProbability(Set<NodeState> key, double probability) {
    if (Double.isNaN(probability)) {
      throw new IllegalArgumentException(
          String.format("Tried to add NaN to probability table %s", tableID));
    }
    vector.getProbabilities()[getIndex(key)] = probability;
  }

  public int getIndex(Set<NodeState> key) {
    return utils.collectIndexesWithStates(getRequestMap(key)).getFirst();
  }
}
