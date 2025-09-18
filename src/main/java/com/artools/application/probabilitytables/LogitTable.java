package com.artools.application.probabilitytables;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import java.util.Map;
import java.util.Set;

public class LogitTable extends ProbabilityTable {
  public static final double ZERO_REPLACEMENT = 1e-9;

  public <T> LogitTable(
      T tableID,
      Map<Set<NodeState>, Double> probabilitiesMap,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions) {
    super(tableID, probabilitiesMap, nodes, events, conditions);
  }

  public double getLogit(Set<NodeState> request) {
    return super.getProbability(request);
  }

  public void setLogit(Set<NodeState> request, double logitVal) {
    super.setProbability(request, logitVal);
  }
}
