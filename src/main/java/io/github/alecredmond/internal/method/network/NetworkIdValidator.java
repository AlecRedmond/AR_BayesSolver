package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;

@Data
public class NetworkIdValidator {
  private final BayesianNetworkData data;

  public void validateNewNode(Node node) {
    if (node == null) {
      throw new BayesNetIDException("Attempted to pass a null value as a Node");
    }
    validateNewIds(
        Stream.concat(Stream.of(node.getId()), node.getNodeStates().stream().map(NodeState::getId))
            .toList());
  }

  public <T extends Serializable> void validateNewIds(Collection<T> newIds) {
    validateNotNull(newIds);
    Set<Serializable> checked = getExistingIDs();
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

  private Set<Serializable> getExistingIDs() {
    return Stream.concat(
            data.getNodeIDsMap().keySet().stream(), data.getNodeStateIDsMap().keySet().stream())
        .collect(Collectors.toCollection(HashSet::new));
  }

  private <T extends Serializable> String stringifyCollection(Collection<T> collection) {
    try {
      return collection.stream().map(T::toString).map(s -> s + " ").collect(Collectors.joining());
    } catch (Exception e) {
      return "[Collection could not be converted to string]";
    }
  }

  public <T extends Serializable, E extends Serializable> void validateNewIds(
      T nodeID, Collection<E> nodeStateIDs) {
    try {
      validateNewIds(Stream.concat(Stream.of(nodeID), nodeStateIDs.stream()).toList());
    } catch (NullPointerException e) {
      throw new BayesNetIDException(e.getMessage());
    }
  }

  public void validateNewId(Serializable id) {
    if (id == null) {
      throw new BayesNetIDException("Attempted to pass a null value as an ID");
    }
    validateNewIds(List.of(id));
  }
}
