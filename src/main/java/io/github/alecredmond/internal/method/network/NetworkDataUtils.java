package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.constraints.NetworkConstraintUtils;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkDataUtils {
  private NetworkDataUtils() {}

  public static <E extends Serializable> Set<Node> getNodesByID(
      Collection<E> nodeIDs, BayesianNetworkData networkData) {
    if (Optional.ofNullable(nodeIDs).isEmpty()) return new HashSet<>();
    return nodeIDs.stream().map(networkData.getNodeIDsMap()::get).collect(Collectors.toSet());
  }

  public static <T extends Serializable> Set<NodeState> getStatesByID(
      Collection<T> nodeStateIDs, BayesianNetworkData networkData) {
    return nodeStateIDs.stream()
        .map(networkData.getNodeStateIDsMap()::get)
        .collect(Collectors.toSet());
  }

  public static <T extends Serializable, E extends Serializable> void addNode(
      T nodeID, Collection<E> nodeStateIDs, BayesianNetworkData networkData) {
    new NetworkIdValidator(networkData).validateNewIds(nodeID, nodeStateIDs);
    Node node = new Node(nodeID, nodeStateIDs);
    networkData.getNodeIDsMap().put(node.getId(), node);
    addStatesToMap(node, networkData);
  }

  private static void addStatesToMap(Node node, BayesianNetworkData networkData) {
    node.getNodeStates()
        .forEach(state -> networkData.getNodeStateIDsMap().put(state.getId(), state));
  }

  static boolean resetAllNodeData(BayesianNetworkData networkData) {
    boolean anyNodesExist = !networkData.getNodeIDsMap().isEmpty();
    networkData.setNodes(new ArrayList<>());
    networkData.setNodeIDsMap(new HashMap<>());
    networkData.setNodeStateIDsMap(new HashMap<>());
    networkData.setNetworkTablesMap(new HashMap<>());
    networkData.setConstraints(new ArrayList<>());
    return anyNodesExist;
  }

  static <T extends Serializable, E extends Serializable> void addNodeStates(
      T nodeID, Collection<E> nodeStateIDs, BayesianNetworkData networkData) {
    new NetworkIdValidator(networkData).validateNewIds(nodeStateIDs);
    nodeStateIDs.forEach(sID -> addNodeState(nodeID, sID, networkData));
  }

  static <T extends Serializable, E extends Serializable> void addNodeState(
      T nodeID, E nodeStateID, BayesianNetworkData networkData) {
    new NetworkIdValidator(networkData).validateNewId(nodeStateID);
    Node node = getNodeByIdOrThrow(nodeID, networkData);
    NodeState state = node.addState(nodeStateID);
    networkData.getNodeStateIDsMap().put(nodeStateID, state);
  }

  static <E extends Serializable> Node getNodeByIdOrThrow(
      E nodeID, BayesianNetworkData networkData) {
    return Optional.ofNullable(networkData.getNodeIDsMap().get(nodeID))
        .orElseThrow(() -> new BayesNetIDException("No Nodes with ID %s found".formatted(nodeID)));
  }

  static void addNode(Node node, BayesianNetworkData networkData) {
    new NetworkIdValidator(networkData).validateNewNode(node);
    networkData.getNodeIDsMap().put(node.getId(), node);
    addStatesToMap(node, networkData);
  }

  static <T extends Serializable> boolean removeNode(T nodeID, BayesianNetworkData networkData) {
    if (!networkData.getNodeIDsMap().containsKey(nodeID)) {
      return false;
    }

    Node toRemove = getNodeByIdOrThrow(nodeID, networkData);

    networkData.getNetworkTablesMap().remove(toRemove);
    networkData.getNodeIDsMap().remove(nodeID);
    removeStatesFromMap(toRemove, networkData);

    List<Node> newNodes = networkData.getNodeIDsMap().values().stream().toList();
    networkData.setNodes(newNodes);

    newNodes.forEach(
        node -> {
          node.getParents().remove(toRemove);
          node.getChildren().remove(toRemove);
        });

    NetworkConstraintUtils.removeAllConstraintsContaining(toRemove, networkData);
    return true;
  }

  private static void removeStatesFromMap(Node toRemove, BayesianNetworkData networkData) {
    toRemove
        .getNodeStates()
        .forEach(state -> networkData.getNodeStateIDsMap().remove(state.getId()));
  }

  static <T extends Serializable> void removeNodeStates(T nodeID, BayesianNetworkData networkData) {
    if (nodeDoesNotExist(nodeID, networkData)) return;
    Node node = getNodeByIdOrThrow(nodeID, networkData);
    List<Serializable> stateIDs = node.getNodeStates().stream().map(NodeState::getId).toList();
    node.setNodeStates(new ArrayList<>());
    stateIDs.forEach(networkData.getNodeStateIDsMap()::remove);
    NetworkConstraintUtils.removeAllConstraintsContaining(node, networkData);
  }

  private static <T extends Serializable> boolean nodeDoesNotExist(
      T nodeID, BayesianNetworkData networkData) {
    return !networkData.getNodeIDsMap().containsKey(nodeID);
  }

  static <T extends Serializable, E extends Serializable> void removeNodeState(
      T nodeID, E nodeStateID, BayesianNetworkData networkData) {
    if (nodeDoesNotExist(nodeID, networkData)) return;
    getNodeByIdOrThrow(nodeID, networkData).removeState(nodeStateID);
    Map<Serializable, NodeState> stateIDsMap = networkData.getNodeStateIDsMap();
    NodeState state = stateIDsMap.get(nodeStateID);
    stateIDsMap.remove(nodeStateID);
    NetworkConstraintUtils.removeAllConstraintsContaining(state, networkData);
  }

  static void addParents(Node child, Collection<Node> parents) {
    parents.forEach(parent -> addParent(child, parent));
  }

  static void addParent(Node child, Node parent) {
    try {
      new NetworkStructureValidator().checkValidRelationship(parent, child);
      child.addParent(parent);
    } catch (NullPointerException e) {
      throw new BayesNetIDException(e.getMessage());
    }
  }

  static <T extends Serializable, E extends Serializable> void addParents(
      T childID, Collection<E> parentIDs, BayesianNetworkData networkData) {
    parentIDs.forEach(pID -> addParent(childID, pID, networkData));
  }

  static <T extends Serializable, E extends Serializable> void addParent(
      T childID, E parentID, BayesianNetworkData networkData) {
    Node parent = getNodeByIdOrThrow(parentID, networkData);
    Node child = getNodeByIdOrThrow(childID, networkData);
    addParent(child, parent);
  }

  static <T extends Serializable> void removeParents(T childID, BayesianNetworkData networkData) {
    removeParents(getNodeByIdOrThrow(childID, networkData));
  }

  static void removeParents(Node child) {
    List<Node> parents = child.getParents();
    child.setParents(new ArrayList<>());
    parents.forEach(parent -> parent.getChildren().remove(child));
  }

  static <T extends Serializable, E extends Serializable> void removeParent(
      T childID, E parentID, BayesianNetworkData networkData) {
    Node parent = getNodeByIdOrThrow(parentID, networkData);
    Node child = getNodeByIdOrThrow(childID, networkData);
    removeParent(child, parent);
  }

  static void removeParent(Node child, Node parent) {
    child.removeParent(parent);
  }
}
