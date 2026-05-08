package io.github.alecredmond.internal.method.network.changehandlers;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import java.beans.PropertyChangeEvent;

public class AddedNodeChangeHandler implements NetworkChangeHandler {
  @Override
  public void applyChange(PropertyChangeEvent evt, BayesianNetworkData networkData) {
    networkData.setSolved(false);
    networkData.getNetworkTablesMap().clear();

    Node node = (Node) evt.getNewValue();

    networkData.getNodeIDsMap().put(node.getId(), node);
    node.getNodeStates()
        .forEach(state -> networkData.getNodeStateIDsMap().put(state.getId(), state));
  }
}
