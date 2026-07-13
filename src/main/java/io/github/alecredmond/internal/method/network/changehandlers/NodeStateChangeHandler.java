package io.github.alecredmond.internal.method.network.changehandlers;

import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.constraints.NetworkConstraintHandler;
import io.github.alecredmond.internal.method.network.validator.NetworkIdValidator;
import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public class NodeStateChangeHandler implements NetworkChangeHandler {
  @Override
  public void applyChange(PropertyChangeEvent evt, BayesianNetworkData networkData) {
    if (networkData.isSolved()) {
      networkData.setSolved(false);
    }
    CollectionChangeAnalyzer<NodeState> analyzer = CollectionChangeAnalyzer.of(evt);
    log.warn(
        "States at Node {} have been changed, will rebuild data and remove invalid constraints...",
        ((Node) evt.getSource()).getId());
    networkData.getNetworkTablesMap().clear();
    rebuildIdMaps(networkData, analyzer);
    removeInvalidConstraints(networkData, analyzer);
  }

  private void rebuildIdMaps(
      BayesianNetworkData networkData, CollectionChangeAnalyzer<NodeState> analyzer) {
    new NetworkIdValidator().validateNewStates(analyzer,networkData);
    Map<Serializable, NodeState> map = networkData.getNodeStateIDsMap();
    analyzer.getRemoved().forEach(r -> map.remove(r.getId()));
    //TODO - ENSURE THIS DOESN'T OVERWRITE OTHER NODE STATES
    analyzer.getAdded().forEach(a -> map.put(a.getId(), a));
  }

  private void removeInvalidConstraints(
      BayesianNetworkData networkData, CollectionChangeAnalyzer<NodeState> analyzer) {
    analyzer
        .getRemoved()
        .forEach(
            state ->
                NetworkConstraintHandler.removeConstraints(
                    constraint -> constraint.getAllStates().contains(state), networkData));
  }
}
