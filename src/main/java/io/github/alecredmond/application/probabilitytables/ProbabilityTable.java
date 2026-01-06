package io.github.alecredmond.application.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class ProbabilityTable {
  protected final Map<Object, NodeState> nodeStateIDMap;
  protected final Map<Object, Node> nodeIDMap;
  protected final Map<Set<NodeState>, Integer> indexMap;
  protected final double[] probabilities;
  protected final Set<Node> nodes;
  protected final Set<Node> events;
  protected final Set<Node> conditions;
  @Setter protected Object tableID;

  protected <T> ProbabilityTable(
      Map<Object, NodeState> nodeStateIDMap,
      Map<Object, Node> nodeIDMap,
      Map<Set<NodeState>, Integer> indexMap,
      double[] probabilities,
      T tableID,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions) {
    this.nodeStateIDMap = nodeStateIDMap;
    this.nodeIDMap = nodeIDMap;
    this.indexMap = indexMap;
    this.probabilities = probabilities;
    this.tableID = tableID;
    this.nodes = nodes;
    this.events = events;
    this.conditions = conditions;
  }

  public <T> NodeState getNodeState(T nodeStateID) {
    return nodeStateIDMap.get(nodeStateID);
  }

  public <T> Node getNode(T nodeID) {
    return nodeIDMap.get(nodeID);
  }

  public <T> double getProbability(Collection<T> stateIDs) {
    return getProbability(getStates(stateIDs));
  }

  public double getProbability(Set<NodeState> key) {
    double probability = probabilities[indexMap.get(key)];
    if (Double.isNaN(probability)) throw new IllegalArgumentException("map returned NaN");
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

  public Set<Set<NodeState>> getKeySet() {
    return indexMap.keySet();
  }

  public void setProbability(Set<NodeState> key, double probability) {
    if (!indexMap.containsKey(key)) {
      throw new IllegalArgumentException(String.format("Illegal set request to table %s", tableID));
    }
    if (Double.isNaN(probability)) throw new IllegalArgumentException("tried to add NaN");
    probabilities[indexMap.get(key)] = probability;
  }

  public int getIndex(Set<NodeState> key) {
    return indexMap.get(key);
  }
}
