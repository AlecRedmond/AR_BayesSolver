package io.github.alecredmond.internal.method.network.changehandlers;

import static io.github.alecredmond.internal.method.constraints.NetworkConstraintUtils.removeAllConstraintsContaining;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.network.NetworkIdValidator;

import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public class NodeStateChangeHandler implements NetworkChangeHandler {
  @Override
  @SuppressWarnings("unchecked")
  public void applyChange(PropertyChangeEvent evt, BayesianNetworkData networkData) {

    Node node = (Node) evt.getSource();
    List<NodeState> oldStates = (List<NodeState>) evt.getOldValue();
    List<NodeState> newStates = (List<NodeState>) evt.getNewValue();
    CollectionChangeAnalyzer<NodeState> analyzer =
        new CollectionChangeAnalyzer<>(oldStates, newStates);

    networkData.setSolved(false);

    log.warn(
        "States at Node {} have been changed, will rebuild data and remove invalid constraints...",
        node.getId());

    networkData.setNetworkTablesMap(new HashMap<>());
    rebuildIdMaps(networkData, analyzer);
    removeInvalidConstraints(networkData, analyzer);
  }

  private void rebuildIdMaps(
      BayesianNetworkData networkData, CollectionChangeAnalyzer<NodeState> analyzer) {
    new NetworkIdValidator(networkData).validateNewStates(analyzer);
    Map<Serializable, NodeState> map = networkData.getNodeStateIDsMap();
    analyzer.getRemoved().forEach(r -> map.remove(r.getId()));
    analyzer.getAdded().forEach(a -> map.put(a.getId(), a));
  }

  private void removeInvalidConstraints(
      BayesianNetworkData networkData, CollectionChangeAnalyzer<NodeState> analyzer) {
    analyzer.getRemoved().forEach(state -> removeAllConstraintsContaining(state, networkData));
  }
}
