package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.NetworkStructureException;
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
import io.github.alecredmond.internal.method.node.NetworkPropertyChangeEvent;
import io.github.alecredmond.internal.method.printer.NetworkPrinter;
import io.github.alecredmond.internal.serialization.BayesianNetworkSerializer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class BayesianNetworkImpl implements BayesianNetwork, PropertyChangeListener {
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

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    NetworkPropertyChangeEvent.valueOf(evt.getPropertyName())
        .getSupplier()
        .get()
        .applyChange(evt, networkData);
  }

  public BayesianNetwork addNode(Node node) throws BayesNetIDException {
    new NetworkIdValidator(networkData).validateNewNode(node);
    node.addPropertyChangeListener(this);
    return this;
  }

  public <T extends Serializable> BayesianNetworkImpl addNewNode(T nodeID)
      throws BayesNetIDException {
    addNode(new Node(nodeID));
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addNewNode(
      T nodeID, Collection<E> nodeStateIDs) throws BayesNetIDException {
    addNode(new Node(nodeID, nodeStateIDs));
    return this;
  }

  public boolean removeNode(Node node) {
    if (node == null) {
      return false;
    }
    return removeNodeByID(node.getId());
  }

  public <T extends Serializable> boolean removeNodeByID(T nodeID) {
    if (nodeID == null) {
      return false;
    }
    Node node = getNode(nodeID);
    if (node == null) {
      return false;
    }
    node.removePropertyChangeListener(this);
    return true;
  }

  public boolean removeAllNodes() {
    Set<Serializable> ids = new HashSet<>(networkData.getNodeIDsMap().keySet());
    ids.forEach(this::removeNodeByID);
    return !ids.isEmpty();
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
    new NetworkDataBuilder(networkData).build();
    return this;
  }

  public <T extends Serializable> ProbabilityTable getNetworkTable(T nodeID) {
    if (!networkData.isSolved()) solveNetwork();
    return networkData.getNetworkTableById(nodeID);
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

  public <E extends Serializable> Set<NodeState> getNodeStates(Collection<E> nodeStateIDs) {
    return nodeStateIDs.stream()
        .map(id -> Optional.ofNullable(getNodeState(id)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
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

  public BayesianNetwork addParents(Node child, Collection<Node> parents)
      throws NetworkStructureException {
    child.setParents(Stream.concat(child.getParents().stream(), parents.stream()).toList());
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addParents(
      T childID, Collection<E> parentIDs) {
    addParents(getNode(childID), getNodes(parentIDs));
    return this;
  }

  public BayesianNetwork addParent(Node child, Node parent) throws NetworkStructureException {
    child.addParent(parent);
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addParent(
      T childID, E parentID) throws NetworkStructureException {
    getNode(childID).addParent(getNode(parentID));
    return this;
  }

  public BayesianNetwork removeParent(Node child, Node parent) {
    child.removeParent(parent);
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl removeParent(
      T childID, E parentID) {
    removeParent(getNode(childID), getNode(parentID));
    return this;
  }

  public BayesianNetwork removeParents(Node child) {
    child.setParents(List.of());
    return this;
  }

  public <T extends Serializable> BayesianNetworkImpl removeParents(T childID) {
    getNode(childID).setParents(List.of());
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addConstraint(
      T eventStateID, Collection<E> conditionStateIDs, double probability) {
    networkData.setSolved(false);
    NetworkConstraintUtils.addConstraint(
        NetworkDataUtils.getNodeStateByIdOrThrow(eventStateID, networkData),
        NetworkDataUtils.getNodeStatesByIdOrThrow(conditionStateIDs, networkData),
        probability,
        networkData);
    return this;
  }

  public <T extends Serializable> BayesianNetworkImpl addConstraint(
      T eventStateID, double probability) {
    networkData.setSolved(false);
    NetworkConstraintUtils.addConstraint(
        NetworkDataUtils.getNodeStateByIdOrThrow(eventStateID, networkData),
        probability,
        networkData);
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
