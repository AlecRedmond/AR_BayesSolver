package io.github.alecredmond.internal.method.network.changehandlers;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;

import java.beans.PropertyChangeEvent;
import java.util.List;

public class NodeChildChangeHandler implements NetworkChangeHandler {
  @Override
  @SuppressWarnings("unchecked")
  public void applyChange(PropertyChangeEvent evt, BayesianNetworkData networkData) {
    Node parent = (Node) evt.getSource();
    List<Node> oldChildren = (List<Node>) evt.getOldValue();
    List<Node> newChildren = (List<Node>) evt.getNewValue();
    CollectionChangeAnalyzer<Node> analyzer =
        new CollectionChangeAnalyzer<>(oldChildren, newChildren);

    networkData.setSolved(false);
    analyzer.getRemoved().forEach(child -> child.removeParent(parent));
    analyzer.getAdded().forEach(child -> child.addParent(parent));
  }
}
