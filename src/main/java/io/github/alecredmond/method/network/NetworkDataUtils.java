package io.github.alecredmond.method.network;

import static io.github.alecredmond.method.probabilitytables.TableBuilder.buildMarginalTable;
import static io.github.alecredmond.method.probabilitytables.TableBuilder.buildNetworkTable;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.export.ProbabilityTable;
import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.exceptions.NetworkStructureException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkDataUtils {
  private NetworkDataUtils() {}

  public static <E> Set<Node> getNodesByID(Collection<E> nodeIDs, BayesianNetworkData networkData) {
    if (Optional.ofNullable(nodeIDs).isEmpty()) return new HashSet<>();
    return nodeIDs.stream().map(networkData.getNodeIDsMap()::get).collect(Collectors.toSet());
  }

  public static <T> Set<NodeState> getStatesByID(
      Collection<T> nodeStateIDs, BayesianNetworkData networkData) {
    return nodeStateIDs.stream()
        .map(networkData.getNodeStateIDsMap()::get)
        .collect(Collectors.toSet());
  }

  static void buildNetworkData(BayesianNetworkData networkData) {
    Map<Node, Integer> layerMap = orderNodes(networkData);
    rebuildIdMaps(networkData);
    buildNetworkTablesMap(layerMap, networkData);
    buildObservedTablesMap(networkData);
    marginalizeAllTables(networkData);
  }

  private static Map<Node, Integer> orderNodes(BayesianNetworkData networkData) {
    Map<Node, Integer> layerMap = new HashMap<>();
    networkData.getNodeIDsMap().values().forEach(node -> calculateNodeLayer(node, layerMap));

    List<Node> nodesOrdered =
        layerMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .toList();

    networkData.setNodes(nodesOrdered);
    return layerMap;
  }

  private static int calculateNodeLayer(Node node, Map<Node, Integer> layerMap) {
    if (layerMap.containsKey(node)) return layerMap.get(node);

    int layer =
        node.getParents().stream()
            .mapToInt(parent -> calculateNodeLayer(parent, layerMap) + 1)
            .max()
            .orElse(0);

    layerMap.put(node, layer);
    return layer;
  }

  private static void rebuildIdMaps(BayesianNetworkData networkData) {
    List<Node> nodes = networkData.getNodes();
    networkData.setNodeIDsMap(createNodeIdMap(nodes));
    networkData.setNodeStateIDsMap(createNodeStateIdMap(nodes));
  }

  private static Map<Object, Node> createNodeIdMap(List<Node> nodes) {
    return nodes.stream()
        .map(n -> Map.entry(n.getId(), n))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map<Object, NodeState> createNodeStateIdMap(List<Node> nodes) {
    return nodes.stream()
        .flatMap(n -> n.getNodeStates().stream())
        .map(ns -> Map.entry(ns.getId(), ns))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static void marginalizeAllTables(BayesianNetworkData networkData) {
    Stream.concat(
            networkData.getNetworkTablesMap().values().stream(),
            networkData.getObservationMap().values().stream())
        .forEach(ProbabilityTable::marginalizeTable);
  }

  static void resetAllNodeData(BayesianNetworkData networkData) {
    networkData.setNodes(new ArrayList<>());
    networkData.setNodeIDsMap(new HashMap<>());
    networkData.setNodeStateIDsMap(new HashMap<>());
    networkData.setNetworkTablesMap(new HashMap<>());
    networkData.setObservationMap(new HashMap<>());
    networkData.setObserved(new HashMap<>());
    networkData.setConstraints(new ArrayList<>());
  }

  static void addNode(Node node, BayesianNetworkData networkData) {
    Object id = node.getId();
    checkForExistingIDs(List.of(id), networkData);
    List<Object> stateIDs = node.getNodeStates().stream().map(NodeState::getId).toList();
    checkForExistingIDs(stateIDs, networkData);
    networkData.getNodeIDsMap().put(node.getId(), node);
    addStatesToMap(node, networkData);
  }

  private static <T> void checkForExistingIDs(Collection<T> ids, BayesianNetworkData networkData) {
    List<T> dupes = new ArrayList<>();
    for (T id : ids) {
      if (networkData.getNodeIDsMap().containsKey(id)
          || networkData.getNodeStateIDsMap().containsKey(id)) {
        dupes.add(id);
      }
    }
    if (!dupes.isEmpty()) {
      throw new BayesNetIDException(
          String.format(
              "Error, found duplicate id(s)! : %s",
              dupes.stream().map(T::toString).map(s -> s + " ").collect(Collectors.joining())));
    }
  }

  private static void addStatesToMap(Node node, BayesianNetworkData networkData) {
    node.getNodeStates()
        .forEach(state -> networkData.getNodeStateIDsMap().put(state.getId(), state));
  }

  private static void buildObservedTablesMap(BayesianNetworkData networkData) {
    networkData.setObservationMap(new HashMap<>());
    networkData
        .getNodes()
        .forEach(
            node -> networkData.getObservationMap().put(node, buildMarginalTable(Set.of(node))));
  }

  private static void buildNetworkTablesMap(
      Map<Node, Integer> layerMap, BayesianNetworkData networkData) {
    networkData.setNetworkTablesMap(new HashMap<>());
    networkData
        .getNodes()
        .forEach(
            node -> {
              List<Node> events = List.of(node);
              List<Node> conditions = orderConditions(node.getParents(), layerMap);
              ProbabilityTable table = buildNetworkTable(events, conditions);
              table.marginalizeTable();
              networkData.getNetworkTablesMap().put(node, table);
            });
  }

  private static List<Node> orderConditions(List<Node> parents, Map<Node, Integer> layerMap) {
    return parents.stream()
        .map(node -> Map.entry(node, layerMap.get(node)))
        .sorted(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .toList();
  }

  static <T> void addNode(T nodeID, BayesianNetworkData networkData) {
    checkForExistingIDs(List.of(nodeID), networkData);
    networkData.getNodeIDsMap().put(nodeID, new Node(nodeID));
  }

  static <T, E> void addNodeStates(
      T nodeID, Collection<E> nodeStateIDs, BayesianNetworkData networkData) {
    checkForExistingIDs(nodeStateIDs, networkData);
    nodeStateIDs.forEach(sID -> addNodeState(nodeID, sID, networkData));
  }

  static <T, E> void addNodeState(T nodeID, E nodeStateID, BayesianNetworkData networkData) {
    checkForExistingIDs(List.of(nodeStateID), networkData);
    Node node = getNodeByID(nodeID, networkData);
    NodeState state = node.addState(nodeStateID);
    networkData.getNodeStateIDsMap().put(nodeStateID, state);
  }

  static <E> Node getNodeByID(E nodeID, BayesianNetworkData networkData) {
    return networkData.getNodeIDsMap().get(nodeID);
  }

  static <T, E> void addNode(
      T nodeID, Collection<E> nodeStateIDs, BayesianNetworkData networkData) {
    List<Object> dupesCheckList = new ArrayList<>(nodeStateIDs);
    checkNoDuplicateStateIDs(nodeID, dupesCheckList);
    dupesCheckList.add(nodeID);
    checkForExistingIDs(dupesCheckList, networkData);
    Node newNode = new Node(nodeID, nodeStateIDs);
    networkData.getNodeIDsMap().put(nodeID, newNode);
    addStatesToMap(newNode, networkData);
  }

  private static void checkNoDuplicateStateIDs(Object nodeID, List<Object> dupesCheckList) {
    Set<Object> objectSet = new HashSet<>(dupesCheckList);
    if (objectSet.size() == dupesCheckList.size()) {
      return;
    }
    throw new ConstraintValidationException(
        String.format("Duplicate state IDs found when building node %s", nodeID.toString()));
  }

  static <T> void removeNode(T nodeID, BayesianNetworkData networkData) {
    if (!networkData.getNodeIDsMap().containsKey(nodeID)) {
      log.error("No node ID '{}' found!", nodeID);
      return;
    }

    Node toRemove = getNodeByID(nodeID, networkData);

    networkData.getNetworkTablesMap().remove(toRemove);
    networkData.getObservationMap().remove(toRemove);
    networkData.getNodeIDsMap().remove(nodeID);
    removeStatesFromMap(toRemove, networkData);

    List<Node> newNodes = networkData.getNodeIDsMap().values().stream().toList();
    networkData.setNodes(newNodes);

    newNodes.forEach(
        node -> {
          node.getParents().remove(toRemove);
          node.getChildren().remove(toRemove);
        });
  }

  private static void removeStatesFromMap(Node toRemove, BayesianNetworkData networkData) {
    toRemove
        .getNodeStates()
        .forEach(state -> networkData.getNodeStateIDsMap().remove(state.getId()));
  }

  static <T> void removeNodeStates(T nodeID, BayesianNetworkData networkData) {
    if (nodeDoesNotExist(nodeID, networkData)) return;
    Node node = getNodeByID(nodeID, networkData);
    List<Object> stateIDs = node.getNodeStates().stream().map(NodeState::getId).toList();
    node.setNodeStates(new ArrayList<>());
    stateIDs.forEach(networkData.getNodeStateIDsMap()::remove);
  }

  private static <T> boolean nodeDoesNotExist(T nodeID, BayesianNetworkData networkData) {
    return !networkData.getNodeIDsMap().containsKey(nodeID);
  }

  static <T, E> void removeNodeState(T nodeID, E nodeStateID, BayesianNetworkData networkData) {
    if (nodeDoesNotExist(nodeID, networkData)) return;
    getNodeByID(nodeID, networkData).removeState(nodeStateID);
    networkData.getNodeStateIDsMap().remove(nodeStateID);
  }

  static void addParents(Node child, Collection<Node> parents) {
    parents.forEach(parent -> addParent(child, parent));
  }

  static void addParent(Node child, Node parent) {
    checkValidRelationship(parent, child);
    child.addParent(parent);
  }

  private static void checkValidRelationship(Node parent, Node child) {
    String error;
    if (parent.equals(child)) {
      error = String.format("Attempted to parent %s with itself!", child);
      throw new NetworkStructureException(error);
    }
    boolean ownAncestor = checkForNetworkLoops(parent, child, Node::getParents);
    if (ownAncestor) {
      error = String.format("Attempted to parent %s with its own ancestor %s", parent, child);
      throw new NetworkStructureException(error);
    }
    boolean ownDescendant = checkForNetworkLoops(child, parent, Node::getChildren);
    if (ownDescendant) {
      error = String.format("Attempted to parent %s with its own descendant %s", child, parent);
      throw new NetworkStructureException(error);
    }
  }

  private static boolean checkForNetworkLoops(
      Node startNode, Node loopConfirm, Function<Node, Collection<Node>> traversalFunction) {
    Set<Node> currentSet = new HashSet<>(traversalFunction.apply(startNode));
    while (!currentSet.isEmpty()) {
      if (currentSet.contains(loopConfirm)) {
        return true;
      }
      Set<Node> nextSet = new HashSet<>();
      currentSet.forEach(node -> nextSet.addAll(traversalFunction.apply(node)));
      currentSet = nextSet;
    }
    return false;
  }

  static <T, E> void addParents(
      T childID, Collection<E> parentIDs, BayesianNetworkData networkData) {
    parentIDs.forEach(pID -> addParent(childID, pID, networkData));
  }

  static <T, E> void addParent(T childID, E parentID, BayesianNetworkData networkData) {
    Node parent = getNodeByID(parentID, networkData);
    Node child = getNodeByID(childID, networkData);
    addParent(child, parent);
  }

  static <T> void removeParents(T childID, BayesianNetworkData networkData) {
    Node child = getNodeByID(childID, networkData);
    removeParents(child);
  }

  static void removeParents(Node child) {
    List<Node> parents = child.getParents();
    child.setParents(new ArrayList<>());
    parents.forEach(parent -> parent.getChildren().remove(child));
  }

  static <T, E> void removeParent(T childID, E parentID, BayesianNetworkData networkData) {
    Node parent = getNodeByID(parentID, networkData);
    Node child = getNodeByID(childID, networkData);
    removeParent(child, parent);
  }

  static void removeParent(Node child, Node parent) {
    child.removeParent(parent);
  }
}
