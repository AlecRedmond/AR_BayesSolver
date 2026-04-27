package io.github.alecredmond.internal.method.network.changehandlers;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.internal.method.constraints.NetworkConstraintUtils;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;

public class RemovedNodeChangeHandler implements NetworkChangeHandler {

  @Override
  public void applyChange(PropertyChangeEvent evt, BayesianNetworkData networkData) {
    networkData.setSolved(false);
    networkData.setNetworkTablesMap(new HashMap<>());

    Node toRemove = (Node) evt.getOldValue();
    networkData.getNetworkTablesMap().remove(toRemove);
    networkData.getNodeIDsMap().remove(toRemove.getId());

    toRemove
        .getNodeStates()
        .forEach(state -> networkData.getNodeStateIDsMap().remove(state.getId()));

    List<Node> newNodes = networkData.getNodeIDsMap().values().stream().toList();
    networkData.setNodes(newNodes);

    newNodes.forEach(
        node -> {
          node.setParents(node.getParents().stream().filter(n -> !n.equals(toRemove)).toList());
          node.setChildren(node.getChildren().stream().filter(n -> !n.equals(toRemove)).toList());
        });

    NetworkConstraintUtils.removeAllConstraintsContaining(toRemove, networkData);
  }
}
