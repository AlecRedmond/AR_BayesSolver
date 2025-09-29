package com.artools.application.probabilitytables;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public abstract class ProbabilityTable {
  protected final Map<Set<NodeState>, Integer> indexMap;
  protected final double[] probabilities;
  protected final Set<Node> nodes;
  protected final Set<Node> events;
  protected final Set<Node> conditions;
  protected Object tableID;

  protected <T> ProbabilityTable(
      Map<Set<NodeState>, Integer> indexMap,
      double[] probabilities,
      T tableID,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions) {
    this.indexMap = indexMap;
    this.probabilities = probabilities;
    this.tableID = tableID;
    this.nodes = nodes;
    this.events = events;
    this.conditions = conditions;
  }

  public double getProbability(Set<NodeState> key, boolean containsRedundant) {
    if (!containsRedundant) return getProbability(key);
    Set<NodeState> validRequest =
        key.stream()
            .filter(state -> nodes.contains(state.getParentNode()))
            .collect(Collectors.toSet());
    return getProbability(validRequest);
  }

  public double getProbability(Set<NodeState> key) {
    double probability = probabilities[indexMap.get(key)];
    if (Double.isNaN(probability)) throw new IllegalArgumentException("map returned NaN");
    return probability;
  }

  public void setProbability(Set<NodeState> key, double probability) {
    if (!indexMap.containsKey(key)) {
      throw new IllegalArgumentException(String.format("Illegal set request to table %s", tableID));
    }
    if (Double.isNaN(probability)) throw new IllegalArgumentException("tried to add NaN");
    probabilities[indexMap.get(key)] = probability;
  }

  public Set<Set<NodeState>> getKeySet() {
    return indexMap.keySet();
  }

  public int getIndex(Set<NodeState> key, boolean containsRedundant) {
    if (!containsRedundant) return indexMap.get(key);
    Set<NodeState> validKey =
        key.stream()
            .filter(state -> nodes.contains(state.getParentNode()))
            .collect(Collectors.toSet());
    return indexMap.get(validKey);
  }
}
