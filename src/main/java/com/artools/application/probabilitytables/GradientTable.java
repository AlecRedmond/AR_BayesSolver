package com.artools.application.probabilitytables;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import java.util.Map;
import java.util.Set;

public class GradientTable extends ProbabilityTable {

  public <T> GradientTable(
      T tableID,
      Map<Set<NodeState>, Double> gradientsMap,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions) {
    super(tableID, gradientsMap, nodes, events, conditions);
  }

  public double getGradient(Set<NodeState> request) {
    return getProbability(request);
  }

  public void setGradient(Set<NodeState> request, double newGradient) {
    setProbability(request, newGradient);
  }
}
