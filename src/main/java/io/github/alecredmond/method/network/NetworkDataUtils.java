package io.github.alecredmond.method.network;

import static io.github.alecredmond.method.probabilitytables.TableBuilder.buildMarginalTable;
import static io.github.alecredmond.method.probabilitytables.TableBuilder.buildNetworkTable;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.exceptions.ParameterConstraintBuilderException;
import io.github.alecredmond.method.probabilitytables.TableUtils;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkDataUtils {
  public final BayesianNetworkData networkData;

  public NetworkDataUtils(BayesianNetworkData networkData) {
    this.networkData = networkData;
  }

  public void buildNetworkData() {
    if (networkData.isSolved()) return;
    orderNodes();
    buildNetworkTablesMap();
    buildObservedTablesMap();
  }

  public void resetAllNodeData() {
    networkData.setNodes(new ArrayList<>());
    networkData.setNodeIDsMap(new HashMap<>());
    networkData.setNodeStateIDsMap(new HashMap<>());
    networkData.setNetworkTablesMap(new HashMap<>());
    networkData.setObservationMap(new HashMap<>());
    networkData.setObservedStatesMap(new HashMap<>());
    networkData.setConstraints(new ArrayList<>());
    networkData.setSolved(false);
    networkData.setJunctionTreeData(null);
  }

  public <T> void addNode(T nodeID) {
    checkForExistingIDs(List.of(nodeID));
    networkData.getNodeIDsMap().put(nodeID, new Node(nodeID));
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

  public <T, E> void addNodeStates(T nodeID, Collection<E> nodeStateIDs) {
    checkForExistingIDs(nodeStateIDs);
    nodeStateIDs.forEach(sID -> addNodeState(nodeID, sID));
  }

  public <T, E> void addNodeState(T nodeID, E nodeStateID) {
    checkForExistingIDs(List.of(nodeStateID));
    Node node = getNodeByID(nodeID);
    NodeState state = node.addState(nodeStateID);
    networkData.getNodeStateIDsMap().put(nodeStateID, state);
    networkData.setSolved(false);
  }

  public <E> Node getNodeByID(E nodeID) {
    return networkData.getNodeIDsMap().get(nodeID);
  }

  public <T, E> void addNode(T nodeID, Collection<E> nodeStateIDs) {
    List<Object> dupesCheckList = new ArrayList<>(nodeStateIDs);
    checkNoDuplicateStateIDs(nodeID, dupesCheckList);
    dupesCheckList.add(nodeID);
    checkForExistingIDs(dupesCheckList);
    Node newNode = new Node(nodeID, nodeStateIDs);
    networkData.getNodes().add(newNode);
    networkData.getNodeIDsMap().put(nodeID, newNode);
    addStatesToMap(newNode);
    networkData.setSolved(false);
  }

  private void checkNoDuplicateStateIDs(Object nodeID, List<Object> dupesCheckList) {
    Set<Object> objectSet = new HashSet<>(dupesCheckList);
    if (objectSet.size() == dupesCheckList.size()) {
      return;
    }
    throw new ParameterConstraintBuilderException(
        String.format("Duplicate state IDs found when building node %s", nodeID.toString()));
  }

  private void addStatesToMap(Node node) {
    node.getNodeStates()
        .forEach(state -> networkData.getNodeStateIDsMap().put(state.getStateID(), state));
  }

  public <T> void removeNode(T nodeID) {
    if (!networkData.getNodeIDsMap().containsKey(nodeID)) {
      log.error("No node ID '{}' found!", nodeID);
      return;
    }
    Node toRemove = getNodeByID(nodeID);

    networkData.getNetworkTablesMap().remove(toRemove);
    networkData.getObservationMap().remove(toRemove);
    networkData.getNodeIDsMap().remove(nodeID);
    removeStatesFromMap(toRemove);

    List<Node> nodes = networkData.getNodes();
    nodes.remove(toRemove);
    nodes.forEach(
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

  public <T> void removeNodeStates(T nodeID) {
    if (nodeDoesNotExist(nodeID)) return;
    Node node = getNodeByID(nodeID);
    List<Object> stateIDs = node.getNodeStates().stream().map(NodeState::getStateID).toList();
    node.setNodeStates(new ArrayList<>());
    stateIDs.forEach(networkData.getNodeStateIDsMap()::remove);
  }

  private <T> boolean nodeDoesNotExist(T nodeID) {
    return !networkData.getNodeIDsMap().containsKey(nodeID);
  }

  public <T, E> void removeNodeState(T nodeID, E nodeStateID) {
    if (nodeDoesNotExist(nodeID)) return;
    getNodeByID(nodeID).removeState(nodeStateID);
    networkData.getNodeStateIDsMap().remove(nodeStateID);
    networkData.setSolved(false);
  }

  public <E> Set<Node> getNodesByID(Collection<E> nodeIDs) {
    if (Optional.ofNullable(nodeIDs).isEmpty()) return new HashSet<>();
    return nodeIDs.stream().map(networkData.getNodeIDsMap()::get).collect(Collectors.toSet());
  }

  public <T> Set<NodeState> getStatesByID(Collection<T> nodeStateIDs) {
    return nodeStateIDs.stream()
        .map(networkData.getNodeStateIDsMap()::get)
        .collect(Collectors.toSet());
  }

  public <T, E> void addParents(T childID, Collection<E> parentIDs) {
    parentIDs.forEach(pID -> addParent(childID, pID));
  }

  public <T, E> void addParent(T childID, E parentID) {
    Node parent = getNodeByID(parentID);
    Node child = getNodeByID(childID);
    checkValidRelationship(parent, child);
    parent.addChild(child);
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

  public <T> void removeParents(T childID) {
    Node child = getNodeByID(childID);
    List<Node> parents = child.getParents();
    child.setParents(new ArrayList<>());
    parents.forEach(parent -> parent.getChildren().remove(child));
  }

  public <T, E> void removeParent(T childID, E parentID) {
    Node parent = getNodeByID(parentID);
    Node child = getNodeByID(childID);
    child.removeParent(parent);
    parent.removeChild(child);
    networkData.setSolved(false);
  }

  private void buildObservedTablesMap() {
    networkData
        .getNodes()
        .forEach(
            node -> networkData.getObservationMap().put(node, buildMarginalTable(Set.of(node))));
  }

  private void buildNetworkTablesMap() {
    networkData
        .getNodes()
        .forEach(
            node -> {
              Set<Node> events = Set.of(node);
              Set<Node> conditions = new HashSet<>(node.getParents());
              ProbabilityTable table = buildNetworkTable(events, conditions);
              TableUtils.marginalizeTable(table);
              networkData.getNetworkTablesMap().put(node, table);
            });
  }

  private void orderNodes() {
    Map<Node, Integer> layerMap = new HashMap<>();
    networkData.getNodes().forEach(node -> calculateNodeLayer(node, layerMap));

    List<Node> nodesOrdered =
        layerMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .toList();

    networkData.setNodes(nodesOrdered);
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
