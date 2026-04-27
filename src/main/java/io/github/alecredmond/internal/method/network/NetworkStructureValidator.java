package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

@Data
public class NetworkStructureValidator {
  public void checkValidRelationship(Node parent, Node child) {
    if (parent.equals(child)) {
      String e = String.format("Attempted to parent %s with itself!", child);
      throw new NetworkStructureException(e);
    }
    if (checkForLoops(parent, child)) {
      String e = String.format("Attempted to parent %s with its own ancestor %s", parent, child);
      throw new NetworkStructureException(e);
    }
  }

  private boolean checkForLoops(Node startNode, Node loopConfirm) {
    Set<Node> currentSet = new HashSet<>(startNode.getParents());
    while (!currentSet.isEmpty()) {
      if (currentSet.contains(loopConfirm)) {
        return true;
      }
      Set<Node> nextSet = new HashSet<>();
      currentSet.forEach(node -> nextSet.addAll(node.getParents()));
      currentSet = nextSet;
    }
    return false;
  }

  public void checkExists(Node node, BayesianNetworkData networkData) {
    if (networkData.getNodeIDsMap().containsKey(node.getId())) {
      return;
    }
    throw new NetworkStructureException("Node %s does not exist!".formatted(node.getId()));
  }
}
