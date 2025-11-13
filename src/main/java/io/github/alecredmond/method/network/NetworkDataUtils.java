package io.github.alecredmond.method.network;

import static io.github.alecredmond.method.probabilitytables.TableBuilder.buildMarginalTable;
import static io.github.alecredmond.method.probabilitytables.TableBuilder.buildNetworkTable;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.ConstraintBuilderException;
import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.method.probabilitytables.TableUtils;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class NetworkDataUtils {
  public final BayesianNetworkData networkData;

  NetworkDataUtils(BayesianNetworkData networkData) {
    this.networkData = networkData;
  }

  void buildNetworkData() {
    if (networkData.isSolved()) return;
    Map<Node, Integer> layerMap = orderNodes();
    rebuildIdMaps();
    buildNetworkTablesMap(layerMap);
    buildObservedTablesMap();
  }

  void resetAllNodeData() {
    networkData.setNodes(new ArrayList<>());
    networkData.setNodeIDsMap(new HashMap<>());
    networkData.setNodeStateIDsMap(new HashMap<>());
    networkData.setNetworkTablesMap(new HashMap<>());
    networkData.setObservationMap(new HashMap<>());
    networkData.setObserved(new HashMap<>());
    networkData.setConstraints(new ArrayList<>());
    networkData.setSolved(false);
  }

  void addNode(Node node) {
    checkForExistingIDs(List.of(node.getNodeID()));
    List<Object> stateIDs = node.getNodeStates().stream().map(NodeState::getStateID).toList();
    checkForExistingIDs(stateIDs);
    networkData.getNodeIDsMap().put(node.getNodeID(), node);
    addStatesToMap(node);
    networkData.setSolved(false);
  }

  private <T> void checkForExistingIDs(Collection<T> ids) {
    List<Object> dupes = new ArrayList<>();
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
              dupes.stream()
                  .map(Object::toString)
                  .map(s -> s + " ")
                  .collect(Collectors.joining())));
    }
  }

  private void addStatesToMap(Node node) {
    node.getNodeStates()
        .forEach(state -> networkData.getNodeStateIDsMap().put(state.getStateID(), state));
  }

  <T> void addNode(T nodeID) {
    checkForExistingIDs(List.of(nodeID));
    networkData.getNodeIDsMap().put(nodeID, new Node(nodeID));
    networkData.setSolved(false);
  }

  <T, E> void addNodeStates(T nodeID, Collection<E> nodeStateIDs) {
    checkForExistingIDs(nodeStateIDs);
    nodeStateIDs.forEach(sID -> addNodeState(nodeID, sID));
  }

  <T, E> void addNodeState(T nodeID, E nodeStateID) {
    checkForExistingIDs(List.of(nodeStateID));
    Node node = getNodeByID(nodeID);
    NodeState state = node.addState(nodeStateID);
    networkData.getNodeStateIDsMap().put(nodeStateID, state);
    networkData.setSolved(false);
  }

  <E> Node getNodeByID(E nodeID) {
    return networkData.getNodeIDsMap().get(nodeID);
  }

  <T, E> void addNode(T nodeID, Collection<E> nodeStateIDs) {
    List<Object> dupesCheckList = new ArrayList<>(nodeStateIDs);
    checkNoDuplicateStateIDs(nodeID, dupesCheckList);
    dupesCheckList.add(nodeID);
    checkForExistingIDs(dupesCheckList);
    Node newNode = new Node(nodeID, nodeStateIDs);
    networkData.getNodeIDsMap().put(nodeID, newNode);
    addStatesToMap(newNode);
    networkData.setSolved(false);
  }

  private void checkNoDuplicateStateIDs(Object nodeID, List<Object> dupesCheckList) {
    Set<Object> objectSet = new HashSet<>(dupesCheckList);
    if (objectSet.size() == dupesCheckList.size()) {
      return;
    }
    throw new ConstraintBuilderException(
        String.format("Duplicate state IDs found when building node %s", nodeID.toString()));
  }

  <T> void removeNode(T nodeID) {
    if (!networkData.getNodeIDsMap().containsKey(nodeID)) {
      log.error("No node ID '{}' found!", nodeID);
      return;
    }
    buildNetworkData();

    Node toRemove = getNodeByID(nodeID);

    networkData.getNetworkTablesMap().remove(toRemove);
    networkData.getObservationMap().remove(toRemove);
    networkData.getNodeIDsMap().remove(nodeID);
    removeStatesFromMap(toRemove);

    List<Node> newNodes = networkData.getNodes().stream().filter(n -> !n.equals(toRemove)).toList();
    networkData.setNodes(newNodes);

    newNodes.forEach(
        node -> {
          node.getParents().remove(toRemove);
          node.getChildren().remove(toRemove);
        });

    networkData.setSolved(false);
  }

  private void removeStatesFromMap(Node toRemove) {
    toRemove
        .getNodeStates()
        .forEach(state -> networkData.getNodeStateIDsMap().remove(state.getStateID()));
  }

  <T> void removeNodeStates(T nodeID) {
    if (nodeDoesNotExist(nodeID)) return;
    Node node = getNodeByID(nodeID);
    List<Object> stateIDs = node.getNodeStates().stream().map(NodeState::getStateID).toList();
    node.setNodeStates(new ArrayList<>());
    stateIDs.forEach(networkData.getNodeStateIDsMap()::remove);
  }

  private <T> boolean nodeDoesNotExist(T nodeID) {
    return !networkData.getNodeIDsMap().containsKey(nodeID);
  }

  <T, E> void removeNodeState(T nodeID, E nodeStateID) {
    if (nodeDoesNotExist(nodeID)) return;
    getNodeByID(nodeID).removeState(nodeStateID);
    networkData.getNodeStateIDsMap().remove(nodeStateID);
    networkData.setSolved(false);
  }

  <E> Set<Node> getNodesByID(Collection<E> nodeIDs) {
    if (Optional.ofNullable(nodeIDs).isEmpty()) return new HashSet<>();
    return nodeIDs.stream().map(networkData.getNodeIDsMap()::get).collect(Collectors.toSet());
  }

  <T> Set<NodeState> getStatesByID(Collection<T> nodeStateIDs) {
    return nodeStateIDs.stream()
        .map(networkData.getNodeStateIDsMap()::get)
        .collect(Collectors.toSet());
  }

  private void rebuildIdMaps() {
    List<Node> nodes = networkData.getNodes();
    networkData.setNodeIDsMap(createNodeIdMap(nodes));
    networkData.setNodeStateIDsMap(createNodeStateIdMap(nodes));
  }

  private Map<Object, Node> createNodeIdMap(List<Node> nodes) {
    return nodes.stream()
        .map(n -> Map.entry(n.getNodeID(), n))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private Map<Object, NodeState> createNodeStateIdMap(List<Node> nodes) {
    return nodes.stream()
        .flatMap(n -> n.getNodeStates().stream())
        .map(ns -> Map.entry(ns.getStateID(), ns))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  void addParents(Node child, Collection<Node> parents) {
    parents.forEach(parent -> addParent(child, parent));
  }

  void addParent(Node child, Node parent) {
    checkValidRelationship(parent, child);
    child.addParent(parent);
    networkData.setSolved(false);
  }

  private void checkValidRelationship(Node parent, Node child) {
    String error;
    if (parent.equals(child)) {
      error = String.format("Attempted to parent %s with itself!", child);
      throw new NetworkStructureException(error);
    }
    boolean ownAncestor = checkReachable(parent, child, Node::getParents);
    if (ownAncestor) {
      error = String.format("Attempted to parent %s with its own ancestor %s", parent, child);
      throw new NetworkStructureException(error);
    }
    boolean ownDescendant = checkReachable(child, parent, Node::getChildren);
    if (ownDescendant) {
      error = String.format("Attempted to parent %s with its own descendant %s", child, parent);
      throw new NetworkStructureException(error);
    }
  }

  private boolean checkReachable(
      Node traversed, Node toCompare, Function<Node, Collection<Node>> function) {
    Set<Node> currentSet = new HashSet<>(function.apply(traversed));
    while (!currentSet.isEmpty()) {
      if (currentSet.contains(toCompare)) {
        return true;
      }
      Set<Node> nextSet = new HashSet<>();
      currentSet.forEach(node -> nextSet.addAll(function.apply(node)));
      currentSet = nextSet;
    }
    return false;
  }

  <T, E> void addParents(T childID, Collection<E> parentIDs) {
    parentIDs.forEach(pID -> addParent(childID, pID));
  }

  <T, E> void addParent(T childID, E parentID) {
    Node parent = getNodeByID(parentID);
    Node child = getNodeByID(childID);
    addParent(child, parent);
  }

  <T> void removeParents(T childID) {
    Node child = getNodeByID(childID);
    removeParents(child);
  }

  void removeParents(Node child) {
    List<Node> parents = child.getParents();
    child.setParents(new ArrayList<>());
    parents.forEach(parent -> parent.getChildren().remove(child));
  }

  <T, E> void removeParent(T childID, E parentID) {
    Node parent = getNodeByID(parentID);
    Node child = getNodeByID(childID);
    removeParent(child, parent);
  }

  void removeParent(Node child, Node parent) {
    child.removeParent(parent);
    networkData.setSolved(false);
  }

  private void buildObservedTablesMap() {
    networkData.setObservationMap(new HashMap<>());
    networkData
        .getNodes()
        .forEach(
            node -> networkData.getObservationMap().put(node, buildMarginalTable(Set.of(node))));
  }

  private void buildNetworkTablesMap(Map<Node, Integer> layerMap) {
    networkData.setNetworkTablesMap(new HashMap<>());
    networkData
        .getNodes()
        .forEach(
            node -> {
              Set<Node> events = Set.of(node);
              Set<Node> conditions = orderConditions(node.getParents(), layerMap);
              ProbabilityTable table = buildNetworkTable(events, conditions);
              TableUtils.marginalizeTable(table);
              networkData.getNetworkTablesMap().put(node, table);
            });
  }

  private Set<Node> orderConditions(List<Node> parents, Map<Node, Integer> layerMap) {
    return parents.stream()
        .map(node -> Map.entry(node, layerMap.get(node)))
        .sorted(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private Map<Node, Integer> orderNodes() {
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

  private int calculateNodeLayer(Node node, Map<Node, Integer> layerMap) {
    if (layerMap.containsKey(node)) return layerMap.get(node);

    int layer =
        node.getParents().stream()
            .mapToInt(parent -> calculateNodeLayer(parent, layerMap) + 1)
            .max()
            .orElse(0);

    layerMap.put(node, layer);
    return layer;
  }
}
