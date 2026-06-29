package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkDataUtils {
  private NetworkDataUtils() {}

  public static <E extends Serializable> Set<Node> getNodesByID(
      Collection<E> nodeIDs, BayesianNetworkData networkData) {
    if (Optional.ofNullable(nodeIDs).isEmpty()) return new HashSet<>();
    return nodeIDs.stream().map(id -> getNodeById(id, networkData)).collect(Collectors.toSet());
  }

  public static <T extends Serializable> Node getNodeById(T id, BayesianNetworkData data) {
    return getById(id, data.getNodeIDsMap(), Node.class, data.getNetworkName());
  }

  public static <T extends Serializable, R> R getById(
      @NonNull T id, Map<Serializable, R> map, Class<R> rClass, String networkName) {
    if (map.containsKey(id)) {
      return map.get(id);
    }
    log.error("No {} with id {} found in Network {}!", rClass.getName(), id, networkName);
    return null;
  }

  public static <T extends Serializable> Set<NodeState> getStatesByID(
      Collection<T> nodeStateIDs, BayesianNetworkData networkData) {
    return nodeStateIDs.stream()
        .map(id -> getStateById(id, networkData))
        .collect(Collectors.toSet());
  }

  public static <T extends Serializable> NodeState getStateById(T id, BayesianNetworkData data) {
    return getById(id, data.getNodeStateIDsMap(), NodeState.class, data.getNetworkName());
  }

  public static void resetAll(BayesianNetworkData networkData) {
    networkData.getNodes().clear();
    networkData.getNodeIDsMap().clear();
    networkData.getNodeStateIDsMap().clear();
    networkData.getNetworkTablesMap().clear();
    networkData.getConstraints().clear();
  }

  static <E extends Serializable> Set<NodeState> getStatesByIdOrThrow(
      Collection<E> stateIds, BayesianNetworkData networkData) {
    return stateIds.stream()
        .map(id -> getStateByIdOrThrow(id, networkData))
        .collect(Collectors.toSet());
  }

  static <T extends Serializable> NodeState getStateByIdOrThrow(
      T stateId, BayesianNetworkData networkData) {
    return Optional.ofNullable(getStateById(stateId, networkData))
        .orElseThrow(
            () -> new BayesNetIDException("No NodeStates with ID %s found".formatted(stateId)));
  }
}
