package io.github.alecredmond.internal.method.network.changehandlers;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import java.beans.PropertyChangeEvent;

public class NodeChildChangeHandler implements NetworkChangeHandler {
  @Override
  public void applyChange(PropertyChangeEvent evt, BayesianNetworkData networkData) {
    CollectionChangeAnalyzer<Node> analyzer = CollectionChangeAnalyzer.of(evt);
    networkData.setSolved(false);
    Node parent = (Node) evt.getSource();
    analyzer.getRemoved().forEach(child -> child.removeParent(parent));
    analyzer.getAdded().forEach(child -> child.addParent(parent));
  }
}
