package io.github.alecredmond.internal.method.inference.solver;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SolverValidator {
  private final BayesianNetwork network;

  public SolverValidator(BayesianNetwork network) {
    this.network = network;
  }

  public void validateDataBuilt() {
    if (checkNetworkBuilt()) return;
    network.buildNetworkData();
  }

  private boolean checkNetworkBuilt() {
    BayesianNetworkData data = network.getNetworkData();
    Map<Node, NetworkTable> tableMap = data.getNetworkTablesMap();
    Set<Node> nodes = new HashSet<>(data.getNodeIDsMap().values());
    for (Node node : nodes) {
      NetworkTable networkTable = tableMap.get(node);
      boolean allCorrect =
          networkTable != null
              && tableContainsAllNodes(networkTable, nodes, node)
              && tableSizeIsCorrect(networkTable);
      if (!allCorrect) return false;
    }
    return true;
  }

  private boolean tableContainsAllNodes(NetworkTable table, Set<Node> allNodes, Node node) {
    Set<Node> nodeParents = new HashSet<>(node.getParents());
    boolean tableNodesInNetwork = allNodes.containsAll(table.getNodes());
    boolean eventNodeCorrect = table.getNetworkNode().equals(node);
    boolean parentRelationshipsValid = table.getConditions().equals(nodeParents);
    return tableNodesInNetwork && eventNodeCorrect && parentRelationshipsValid;
  }

  private boolean tableSizeIsCorrect(NetworkTable table) {
    ProbabilityVector vector = table.getVector();
    Node[] nodes = vector.getNodeArray();
    NodeState[][] states = vector.getStateArrays();
    for (int i = 0; i < nodes.length; i++) {
      if (states[i].length != nodes[i].getNodeStates().size()) return false;
    }
    return true;
  }

  public void resetNetworkTables() {
    network
        .getNetworkTables()
        .values()
        .forEach(
            table -> {
              Arrays.fill(table.getProbabilities(), 1.0);
              table.getQueryTool().normalizeTable();
            });
  }
}
