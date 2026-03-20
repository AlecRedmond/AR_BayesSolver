package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.method.inference.BayesSolver;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.serialization.network.SerializedBayesianNetwork;
import io.github.alecredmond.internal.fileio.NetworkFileIO;
import io.github.alecredmond.internal.method.constraints.NetworkConstraintUtils;
import io.github.alecredmond.internal.method.printer.NetworkPrinter;
import io.github.alecredmond.internal.serialization.BayesianNetworkSerializer;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class BayesianNetworkImpl implements BayesianNetwork {
  private final BayesianNetworkData networkData;

  public BayesianNetworkImpl(String networkName) {
    this.networkData = new BayesianNetworkData();
    networkData.setNetworkName(networkName);
  }

  public BayesianNetworkImpl() {
    this.networkData = new BayesianNetworkData();
  }

  public BayesianNetworkImpl(BayesianNetworkData networkData) {
    this.networkData = networkData;
  }

  public boolean saveNetworkToFile(File file) {
    return new NetworkFileIO(new BayesianNetworkSerializer()).saveNetwork(this, file);
  }

  public boolean saveNetworkToFile(String filePath) {
    return new NetworkFileIO(new BayesianNetworkSerializer()).saveNetwork(this, filePath);
  }

  public boolean saveNetworkToFile() {
    return new NetworkFileIO(new BayesianNetworkSerializer()).saveNetwork(this);
  }

  public SerializedBayesianNetwork serializeNetwork() {
    return new BayesianNetworkSerializer().serialize(this);
  }

  public BayesianNetwork addNode(Node node) {
    networkData.setSolved(false);
    NetworkDataUtils.addNode(node, networkData);
    return this;
  }

  public <T extends Serializable> BayesianNetworkImpl addNewNode(T nodeID) {
    networkData.setSolved(false);
    NetworkDataUtils.addNode(nodeID, networkData);
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addNewNode(
      T nodeID, Collection<E> nodeStateIDs) {
    networkData.setSolved(false);
    NetworkDataUtils.addNode(nodeID, nodeStateIDs, networkData);
    return this;
  }

  public BayesianNetwork removeNode(Node node) {
    networkData.setSolved(false);
    if (Optional.ofNullable(node).isEmpty()) return this;
    NetworkDataUtils.removeNode(node.getId(), networkData);
    return null;
  }

  public <T extends Serializable> BayesianNetworkImpl removeNodeByID(T nodeID) {
    networkData.setSolved(false);
    NetworkDataUtils.removeNode(nodeID, networkData);
    return this;
  }

  public BayesianNetworkImpl removeAllNodes() {
    networkData.setSolved(false);
    NetworkDataUtils.resetAllNodeData(networkData);
    return this;
  }

  public BayesianNetworkImpl solveNetwork() {
    if (networkData.isSolved()) {
      return this;
    }
    BayesSolver.create(this).solve();
    return this;
  }

  public BayesianNetworkImpl printNetwork() {
    if (!networkData.isSolved()) solveNetwork();
    new NetworkPrinter(networkData).printNetwork();
    return this;
  }

  public BayesianNetwork buildNetworkData() {
    NetworkDataUtils.buildNetworkData(networkData);
    return this;
  }

  public <T extends Serializable> ProbabilityTable getNetworkTable(T nodeID) {
    if (!networkData.isSolved()) solveNetwork();
    return Optional.ofNullable(networkData.getNetworkTable(nodeID)).orElseThrow();
  }

  @Override
  public InferenceEngine buildInferenceEngine() {
    return InferenceEngine.create(this);
  }

  public <T extends Serializable> Node getNode(T nodeID) {
    Map<Serializable, Node> map = networkData.getNodeIDsMap();
    if (map.containsKey(nodeID)) {
      return map.get(nodeID);
    }
    log.error("No node with ID {} found in network {}!", nodeID, networkData.getNetworkName());
    return null;
  }

  public <T extends Serializable> Set<Node> getNodes(Collection<T> nodeIDs) {
    return nodeIDs.stream().map(this::getNode).collect(Collectors.toSet());
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addNodeStates(
      T nodeID, Collection<E> nodeStateIDs) {
    networkData.setSolved(false);
    NetworkDataUtils.addNodeStates(nodeID, nodeStateIDs, networkData);
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addNodeState(
      T nodeID, E nodeStateID) {
    networkData.setSolved(false);
    NetworkDataUtils.addNodeState(nodeID, nodeStateID, networkData);
    return this;
  }

  public <T extends Serializable> BayesianNetworkImpl removeNodeStates(T nodeID) {
    networkData.setSolved(false);
    NetworkDataUtils.removeNodeStates(nodeID, networkData);
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl removeNodeState(
      T nodeID, E nodeStateID) {
    networkData.setSolved(false);
    NetworkDataUtils.removeNodeState(nodeID, nodeStateID, networkData);
    return this;
  }

  public <E extends Serializable> Set<NodeState> getNodeStates(Collection<E> nodeStateIDs) {
    return nodeStateIDs.stream().map(this::getNodeState).collect(Collectors.toSet());
  }

  public <E extends Serializable> NodeState getNodeState(E nodeStateID) {
    Map<Serializable, NodeState> map = networkData.getNodeStateIDsMap();
    if (map.containsKey(nodeStateID)) {
      return map.get(nodeStateID);
    }
    log.error(
        "No node state with ID {} found in network {}!", nodeStateID, networkData.getNetworkName());
    return null;
  }

  public BayesianNetwork addParents(Node child, Collection<Node> parents) {
    networkData.setSolved(false);
    NetworkDataUtils.addParents(child, parents);
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addParents(
      T childID, Collection<E> parentIDs) {
    networkData.setSolved(false);
    NetworkDataUtils.addParents(childID, parentIDs, networkData);
    return this;
  }

  public BayesianNetwork addParent(Node child, Node parent) {
    networkData.setSolved(false);
    NetworkDataUtils.addParent(child, parent);
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addParent(
      T childID, E parentID) {
    networkData.setSolved(false);
    NetworkDataUtils.addParent(childID, parentID, networkData);
    return this;
  }

  public BayesianNetwork removeParent(Node child, Node parent) {
    networkData.setSolved(false);
    NetworkDataUtils.removeParent(child, parent);
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl removeParent(
      T childID, E parentID) {
    networkData.setSolved(false);
    NetworkDataUtils.removeParent(childID, parentID, networkData);
    return this;
  }

  public BayesianNetwork removeParents(Node child) {
    networkData.setSolved(false);
    NetworkDataUtils.removeParents(child);
    return this;
  }

  public <T extends Serializable> BayesianNetworkImpl removeParents(T childID) {
    networkData.setSolved(false);
    NetworkDataUtils.removeParents(childID, networkData);
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addConstraint(
      T eventStateID, Collection<E> conditionStateIDs, double probability) {
    networkData.setSolved(false);
    NetworkConstraintUtils.addConstraint(
        getNodeState(eventStateID), getNodeStates(conditionStateIDs), probability, networkData);
    return this;
  }

  public <T extends Serializable> BayesianNetworkImpl addConstraint(
      T eventStateID, double probability) {
    networkData.setSolved(false);
    NetworkConstraintUtils.addConstraint(getNodeState(eventStateID), probability, networkData);
    return this;
  }

  public BayesianNetwork addConstraint(ProbabilityConstraint probabilityConstraint) {
    networkData.setSolved(false);
    NetworkConstraintUtils.addConstraint(probabilityConstraint, networkData);
    return this;
  }

  public BayesianNetwork addConstraints(Collection<ProbabilityConstraint> probabilityConstraints) {
    networkData.setSolved(false);
    NetworkConstraintUtils.addConstraints(probabilityConstraints, networkData);
    return this;
  }

  public <T extends Serializable> MarginalConstraint getConstraint(T eventStateId) {
    return NetworkConstraintUtils.getConstraint(getNodeState(eventStateId), networkData);
  }

  public <T extends Serializable, E extends Serializable> ProbabilityConstraint getConstraint(
      T eventStateId, Collection<E> conditionStateIds) {
    return NetworkConstraintUtils.getConstraint(
        getNodeState(eventStateId), getNodeStates(conditionStateIds), networkData);
  }

  public boolean removeConstraint(ProbabilityConstraint probabilityConstraint) {
    networkData.setSolved(false);
    return NetworkConstraintUtils.removeConstraint(probabilityConstraint, networkData);
  }

  public <T extends Serializable> boolean removeConstraint(T eventStateId) {
    networkData.setSolved(false);
    return NetworkConstraintUtils.removeConstraint(getNodeState(eventStateId), networkData);
  }

  public <T extends Serializable, E extends Serializable> boolean removeConstraint(
      T eventStateId, Collection<E> conditionStateIds) {
    networkData.setSolved(false);
    return NetworkConstraintUtils.removeConstraint(
        getNodeState(eventStateId), getNodeStates(conditionStateIds), networkData);
  }

  public boolean removeAllConstraints() {
    networkData.setSolved(false);
    return NetworkConstraintUtils.removeAllConstraints(networkData);
  }
}
