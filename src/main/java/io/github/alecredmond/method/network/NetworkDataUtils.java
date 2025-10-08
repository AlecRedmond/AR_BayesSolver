package io.github.alecredmond.method.network;

import static io.github.alecredmond.method.probabilitytables.TableBuilder.buildMarginalTable;
import static io.github.alecredmond.method.probabilitytables.TableBuilder.buildNetworkTable;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.method.probabilitytables.TableUtils;
import java.util.*;
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
    checkForDuplicateIDs(List.of(nodeID));
    networkData.getNodeIDsMap().put(nodeID, new Node(nodeID));
    networkData.setSolved(false);
  }

  private <T> void checkForDuplicateIDs(Collection<T> ids) {
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
    checkForDuplicateIDs(nodeStateIDs);
    nodeStateIDs.forEach(sID -> addNodeState(nodeID, sID));
  }

  public <T, E> void addNodeState(T nodeID, E nodeStateID) {
    checkForDuplicateIDs(List.of(nodeStateID));
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
    dupesCheckList.add(nodeID);
    checkForDuplicateIDs(dupesCheckList);
    Node newNode = new Node(nodeID, nodeStateIDs);
    networkData.getNodes().add(newNode);
    networkData.getNodeIDsMap().put(nodeID, newNode);
    addStatesToMap(newNode);
    networkData.setSolved(false);
  }

  private void addStatesToMap(Node node) {
    node.getStates()
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
    networkData.setSolved(false);
  }

  private void removeStatesFromMap(Node toRemove) {
    toRemove
        .getStates()
        .forEach(state -> networkData.getNodeStateIDsMap().remove(state.getStateID()));
  }

  public <T> void removeNodeStates(T nodeID) {
    getNodeByID(nodeID).getStates().stream()
        .map(NodeState::getStateID)
        .forEach(sID -> removeNodeState(nodeID, sID));
  }

  public <T, E> void removeNodeState(T nodeID, E nodeStateID) {
    getNodeByID(nodeID).removeState(nodeStateID);
    networkData.getNodeStateIDsMap().remove(nodeStateID);
    networkData.setSolved(false);
  }

  public <E> Set<Node> getNodesByID(Collection<E> nodeIDs) {
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
    parent.addChild(child);
    child.addParent(parent);
    networkData.setSolved(false);
  }

  public <T> void removeParents(T childID) {
    getNodeByID(childID).getParents().stream()
        .map(Node::getNodeID)
        .forEach(parentID -> removeParent(childID, parentID));
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
