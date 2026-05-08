package io.github.alecredmond.internal.method.network.changehandlers;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.internal.method.network.NetworkStructureValidator;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.beans.PropertyChangeEvent;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NodeParentChangeHandler implements NetworkChangeHandler {
  @Override
  public void applyChange(PropertyChangeEvent evt, BayesianNetworkData networkData) {
    NetworkStructureValidator validator = new NetworkStructureValidator();
    CollectionChangeAnalyzer<Node> analyzer = CollectionChangeAnalyzer.of(evt);

    networkData.setSolved(false);
    networkData.getNetworkTablesMap().clear();

    Node child = (Node) evt.getSource();
    analyzer
        .getAdded()
        .forEach(
            parent -> {
              validator.checkExists(parent, networkData);
              validator.checkValidRelationship(parent, child);
              NodeUtils.addParent(child, parent);
            });
  }
}
