package com.artools.application.probabilitytables;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public abstract class ProbabilityTable {
  protected final Map<Set<NodeState>, Double> probabilitiesMap;
  protected final Set<Node> nodes;
  protected final Set<Node> events;
  protected final Set<Node> conditions;
  protected Object tableID;

  protected <T> ProbabilityTable(
      T tableID,
      Map<Set<NodeState>, Double> probabilitiesMap,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions) {
    this.tableID = tableID;
    this.probabilitiesMap = probabilitiesMap;
    this.nodes = nodes;
    this.events = events;
    this.conditions = conditions;
  }

  public double getProbability(Set<NodeState> request, boolean containsRedundant) {
    if (!containsRedundant) return getProbability(request);
    Set<NodeState> validRequest =
        request.stream()
            .filter(state -> nodes.contains(state.getParentNode()))
            .collect(Collectors.toSet());
    return getProbability(validRequest);
  }

  public double getProbability(Set<NodeState> states) {
    double probability = probabilitiesMap.get(states);
    if (Double.isNaN(probability)) throw new IllegalArgumentException("map returned NaN");
    return probability;
  }

  public void setProbability(Set<NodeState> states, double probability) {
    if (!probabilitiesMap.containsKey(states)) {
      throw new IllegalArgumentException(String.format("Illegal set request to table %s", tableID));
    }
    if (Double.isNaN(probability)) throw new IllegalArgumentException("tried to add NaN");
    probabilitiesMap.put(states, probability);
  }

  public Set<Set<NodeState>> getKeySet(){
      return probabilitiesMap.keySet();
  }
}
