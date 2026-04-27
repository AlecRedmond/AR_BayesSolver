package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.network.changehandlers.CollectionChangeAnalyzer;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class NetworkIdValidator {
  private final BayesianNetworkData data;

  public void validateNewNode(Node node) {
    if (node == null) {
      throw new BayesNetIDException("Attempted to pass a null value as a Node");
    }
    Serializable nodeId = node.getId();
    List<Serializable> stateIds = node.getNodeStates().stream().map(NodeState::getId).toList();
    validateNewIds(nodeId, stateIds);
  }

  public Set<Serializable> validateExistingData() {
    Set<Serializable> checked = new HashSet<>();
    validateIdCollection(data.getNodeIDsMap().keySet(), checked);
    validateIdCollection(data.getNodeStateIDsMap().keySet(), checked);
    return checked;
  }

  public Set<Serializable> validateRebuild(Collection<Node> nodes) {
    Set<Serializable> checked = new HashSet<>();
    List<Serializable> nodeIds = NodeUtils.getNodeIds(nodes);
    List<Serializable> nodeStateIds = NodeUtils.getNodeStateIds(nodes);
    validateIdCollection(nodeIds, checked);
    validateIdCollection(nodeStateIds, checked);
    return checked;
  }

  private <T extends Serializable> void validateIdCollection(
      Collection<T> newIds, Set<Serializable> checked) {
    validateNotNull(newIds);
    List<T> dupes = newIds.stream().filter(id -> !checked.add(id)).toList();
    if (dupes.isEmpty()) {
      return;
    }
    throw new BayesNetIDException(
        String.format("Error, found duplicate id(s)! : %s", stringifyCollection(dupes)));
  }

  private <T extends Serializable> void validateNotNull(Collection<T> newIds) {
    if (newIds.stream().map(Optional::ofNullable).anyMatch(Optional::isEmpty)) {
      throw new BayesNetIDException(
          "Input IDs contained a null value: %s".formatted(stringifyCollection(newIds)));
    }
  }

  private <T extends Serializable> String stringifyCollection(Collection<T> collection) {
    try {
      return collection.stream().map(T::toString).map(s -> s + " ").collect(Collectors.joining());
    } catch (Exception e) {
      return "[Collection could not be converted to string]";
    }
  }

  public void validateNewStates(CollectionChangeAnalyzer<NodeState> analyzer) {
    Set<Serializable> dupes =
        analyzer.getDupesInNew().stream()
            .map(NodeState::getId)
            .collect(Collectors.toCollection(HashSet::new));

    if (!dupes.isEmpty()) {
      throw new BayesNetIDException(
          String.format("Error, found duplicate id(s)! : %s", stringifyCollection(dupes)));
    }

    Set<Serializable> added =
        analyzer.getAdded().stream().map(NodeState::getId).collect(Collectors.toSet());

    validateIdCollection(added, validateExistingData());
  }

  public <T extends Serializable, E extends Serializable> void validateNewIds(
      T nodeID, Collection<E> nodeStateIDs) {
    try {
      List<Serializable> list = new ArrayList<>(nodeStateIDs);
      list.add(nodeID);
      validateIdCollection(list, validateExistingData());
    } catch (NullPointerException e) {
      throw new BayesNetIDException(e.getMessage());
    }
  }
}
