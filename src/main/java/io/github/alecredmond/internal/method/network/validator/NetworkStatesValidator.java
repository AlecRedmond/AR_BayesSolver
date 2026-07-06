package io.github.alecredmond.internal.method.network.validator;

import io.github.alecredmond.exceptions.NodeStateValidationException;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NetworkStatesValidator implements NetworkValidator {

  @Override
  public void validateData(BayesianNetworkData networkData) {
    Set<Serializable> checkedStateIds = new HashSet<>();
    Set<NodeState> allDeclaredStates = new HashSet<>(networkData.getNodeStateIDsMap().values());
    networkData
        .getNodeIDsMap()
        .values()
        .forEach(
            node -> {
              statesArePresentAndNotEmpty(node);
              stateIdsAreNotDuplicated(node, checkedStateIds);
              statesAreDeclaredInTheNetwork(node, allDeclaredStates);
            });
  }

  private void statesArePresentAndNotEmpty(Node node) {
    List<NodeState> stateList = node.getNodeStates();
    if (stateList == null) {
      throw new NodeStateValidationException("Node %s : NodeState list is null!".formatted(node));
    }
    if (stateList.isEmpty()) {
      throw new NodeStateValidationException("Node %s : NodeState list is empty!".formatted(node));
    }
  }

  private void stateIdsAreNotDuplicated(Node node, Set<Serializable> checkedStateIds) {
    List<NodeState> duplicates =
        node.getNodeStates().stream()
            .filter(nodeState -> !checkedStateIds.add(nodeState.getId()))
            .toList();
    if (duplicates.isEmpty()) return;
    throw new NodeStateValidationException(
        "Node %s : Duplicated NodeStates ids found = [%s]"
            .formatted(node, NodeUtils.formatStatesToString(duplicates)));
  }

  private void statesAreDeclaredInTheNetwork(Node node, Set<NodeState> allDeclaredStates) {
    List<NodeState> undeclared =
        node.getNodeStates().stream().filter(ns -> !allDeclaredStates.contains(ns)).toList();
    if (undeclared.isEmpty()) return;
    throw new NodeStateValidationException(
        "Node %s : Undeclared NodeStates found = [%s]"
            .formatted(node, NodeUtils.formatStatesToString(undeclared)));
  }
}
