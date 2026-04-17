package io.github.alecredmond.internal.method.network.changehandlers;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.internal.method.network.NetworkStructureValidator;
import io.github.alecredmond.internal.method.node.NodeUtils;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;

import io.github.alecredmond.internal.method.utils.CollectionChangeAnalyzer;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NodeParentChangeHandler implements NetworkChangeHandler {
  @Override
  @SuppressWarnings("unchecked")
  public void applyChange(PropertyChangeEvent evt, BayesianNetworkData networkData) {
    NetworkStructureValidator validator = new NetworkStructureValidator();
    Node child = (Node) evt.getSource();
    List<Node> oldParents = (List<Node>) evt.getOldValue();
    List<Node> newParents = (List<Node>) evt.getNewValue();
    CollectionChangeAnalyzer<Node> analyzer =
        new CollectionChangeAnalyzer<>(oldParents, newParents);

    if (analyzer.getRemoved().isEmpty() && analyzer.getAdded().isEmpty()) {
      return;
    }

    networkData.setSolved(false);
    networkData.setNetworkTablesMap(new HashMap<>());

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
