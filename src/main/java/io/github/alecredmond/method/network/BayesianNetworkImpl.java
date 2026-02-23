package io.github.alecredmond.method.network;

import io.github.alecredmond.application.constraints.Constraint;
import io.github.alecredmond.application.constraints.MarginalConstraint;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.method.inference.InferenceEngine;
import io.github.alecredmond.method.printer.NetworkPrinter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
class BayesianNetworkImpl implements BayesianNetwork {
  private final BayesianNetworkData networkData;
  private final NetworkNodeUtils utils;
  private final InferenceEngine inferenceEngine;

  BayesianNetworkImpl(String networkName) {
    this.networkData = new BayesianNetworkData();
    networkData.setNetworkName(networkName);
    this.utils = new NetworkNodeUtils(networkData);
    this.inferenceEngine = new InferenceEngine(networkData);
  }

  BayesianNetworkImpl() {
    this.networkData = new BayesianNetworkData();
    this.utils = new NetworkNodeUtils(networkData);
    this.inferenceEngine = new InferenceEngine(networkData);
  }

  @Override
  public BayesianNetwork addNode(Node node) {
    utils.addNode(node);
    return this;
  }

  public <T> BayesianNetworkImpl addNode(T nodeID) {
    utils.addNode(nodeID);
    return this;
  }

  public <T, E> BayesianNetworkImpl addNode(T nodeID, Collection<E> nodeStateIDs) {
    utils.addNode(nodeID, nodeStateIDs);
    return this;
  }

  @Override
  public BayesianNetwork removeNode(Node node) {
    if (Optional.ofNullable(node).isEmpty()) return this;
    utils.removeNode(node.getId());
    return null;
  }

  public <T> BayesianNetworkImpl removeNode(T nodeID) {
    utils.removeNode(nodeID);
    return this;
  }

  public BayesianNetworkImpl removeAllNodes() {
    utils.resetAllNodeData();
    return this;
  }

  @Override
  public <T> Node getNode(T nodeID) {
    Map<Object, Node> map = networkData.getNodeIDsMap();
    if (map.containsKey(nodeID)) {
      return map.get(nodeID);
    }
    throw new IllegalArgumentException("No node with ID " + nodeID + " found in network");
  }

  public <T, E> BayesianNetworkImpl addNodeStates(T nodeID, Collection<E> nodeStateIDs) {
    utils.addNodeStates(nodeID, nodeStateIDs);
    return this;
  }

  public <T, E> BayesianNetworkImpl addNodeState(T nodeID, E nodeStateID) {
    utils.addNodeState(nodeID, nodeStateID);
    return this;
  }

  public <T> BayesianNetworkImpl removeNodeStates(T nodeID) {
    utils.removeNodeStates(nodeID);
    return this;
  }

  public <T, E> BayesianNetworkImpl removeNodeState(T nodeID, E nodeStateID) {
    utils.removeNodeState(nodeID, nodeStateID);
    return this;
  }

  public <E> Set<NodeState> getNodeStates(Collection<E> nodeStateIDs) {
    return nodeStateIDs.stream().map(this::getNodeState).collect(Collectors.toSet());
  }

  @Override
  public <E> NodeState getNodeState(E nodeStateID) {
    Map<Object, NodeState> map = networkData.getNodeStateIDsMap();
    if (map.containsKey(nodeStateID)) {
      return map.get(nodeStateID);
    }
    throw new IllegalArgumentException("No node with ID " + nodeStateID + " found in network");
  }

  @Override
  public BayesianNetwork addParents(Node child, Collection<Node> parents) {
    utils.addParents(child, parents);
    return this;
  }

  public <T, E> BayesianNetworkImpl addParents(T childID, Collection<E> parentIDs) {
    utils.addParents(childID, parentIDs);
    return this;
  }

  @Override
  public BayesianNetwork addParent(Node child, Node parent) {
    utils.addParent(child, parent);
    return this;
  }

  public <T, E> BayesianNetworkImpl addParent(T childID, E parentID) {
    utils.addParent(childID, parentID);
    return this;
  }

  @Override
  public BayesianNetwork removeParent(Node child, Node parent) {
    utils.removeParent(child, parent);
    return this;
  }

  public <T, E> BayesianNetworkImpl removeParent(T childID, E parentID) {
    utils.removeParent(childID, parentID);
    return this;
  }

  @Override
  public BayesianNetwork removeParents(Node child) {
    utils.removeParents(child);
    return this;
  }

  public <T> BayesianNetworkImpl removeParents(T childID) {
    utils.removeParents(childID);
    return this;
  }

  public <T, E> BayesianNetworkImpl addConstraint(
      T eventStateID, Collection<E> conditionStateIDs, double probability) {
    networkData.setSolved(false);
    NetworkConstraintUtils.addConstraint(
        getNodeState(eventStateID),
        getNodeStates(conditionStateIDs),
        probability,
        networkData);
    return this;
  }

  public <T> BayesianNetworkImpl addConstraint(T eventStateID, double probability) {
    networkData.setSolved(false);
    NetworkConstraintUtils.addConstraint(getNodeState(eventStateID), probability, networkData);
    return this;
  }

  @Override
  public BayesianNetwork addConstraint(Constraint constraint) {
    networkData.setSolved(false);
    NetworkConstraintUtils.addConstraint(constraint, networkData);
    return this;
  }

  @Override
  public BayesianNetwork addConstraints(Collection<Constraint> constraints) {
    networkData.setSolved(false);
    NetworkConstraintUtils.addConstraints(constraints, networkData);
    return this;
  }

  @Override
  public <T> MarginalConstraint getConstraint(T eventStateId) {
    return NetworkConstraintUtils.getConstraint(getNodeState(eventStateId), networkData);
  }

  @Override
  public <T, E> Constraint getConstraint(T eventStateId, Collection<E> conditionStateIds) {
    return NetworkConstraintUtils.getConstraint(
        getNodeState(eventStateId),
        getNodeStates(conditionStateIds),
        networkData);
  }

  @Override
  public boolean removeConstraint(Constraint constraint) {
    networkData.setSolved(false);
    return NetworkConstraintUtils.removeConstraint(constraint, networkData);
  }

  @Override
  public <T> boolean removeConstraint(T eventStateId) {
    networkData.setSolved(false);
    return NetworkConstraintUtils.removeConstraint(getNodeState(eventStateId), networkData);
  }

  @Override
  public <T, E> boolean removeConstraint(T eventStateId, Collection<E> conditionStateIds) {
    networkData.setSolved(false);
    return NetworkConstraintUtils.removeConstraint(
        getNodeState(eventStateId),
        getNodeStates(conditionStateIds),
        networkData);
  }

  @Override
  public boolean removeAllConstraints() {
    networkData.setSolved(false);
    return NetworkConstraintUtils.removeAllConstraints(networkData);
  }

  public BayesianNetworkImpl solveNetwork() {
    if (networkData.isSolved()) {
      return this;
    }
    utils.buildNetworkData();
    inferenceEngine.runSolver();
    return this;
  }

  public BayesianNetworkImpl printObserved() {
    if (!networkData.isSolved()) solveNetwork();
    new NetworkPrinter(networkData).printObserved();
    return this;
  }

  public BayesianNetworkImpl printNetwork() {
    if (!networkData.isSolved()) solveNetwork();
    new NetworkPrinter(networkData).printNetwork();
    return this;
  }

  public <T> BayesianNetworkImpl observeNetwork(Collection<T> observedNodeStateIDs) {
    if (!networkData.isSolved()) solveNetwork();
    inferenceEngine.observeNetwork(utils.getStatesByID(observedNodeStateIDs));
    return this;
  }

  public BayesianNetworkImpl observeMarginals() {
    if (!networkData.isSolved()) {
      solveNetwork();
      return this;
    }
    inferenceEngine.observeNetwork(List.of());
    return this;
  }

  @Override
  public BayesianNetwork buildNetworkData() {
    utils.buildNetworkData();
    return this;
  }

  @Override
  public BayesianNetworkData getNetworkData() {
    return networkData;
  }

  public <T, E> List<List<T>> generateSamples(
      Collection<E> excludeNodeIDs,
      Collection<E> includeNodeIDs,
      int numberOfSamples,
      Class<T> sampleClass) {
    if (!networkData.isSolved()) solveNetwork();
    return inferenceEngine.generateSamples(
        utils.getNodesByID(excludeNodeIDs),
        utils.getNodesByID(includeNodeIDs),
        numberOfSamples,
        sampleClass);
  }

  @Override
  public <T, E> List<List<T>> generateSamples(
      Collection<E> includeNodeIDs, int numberOfSamples, Class<T> sampleClass) {
    return generateSamples(List.of(), includeNodeIDs, numberOfSamples, sampleClass);
  }

  @Override
  public <T> List<List<T>> generateSamples(int numberOfSamples, Class<T> sampleClass) {
    return generateSamples(List.of(), List.of(), numberOfSamples, sampleClass);
  }

  public <T> double getProbabilityFromCurrentObservations(Collection<T> eventStateIDs) {
    if (!networkData.isSolved()) solveNetwork();
    return inferenceEngine.getProbabilityFromCurrentObservations(
        utils.getStatesByID(eventStateIDs));
  }

  public <T> ProbabilityTable getNetworkTable(T nodeID) {
    if (!networkData.isSolved()) solveNetwork();
    return Optional.ofNullable(networkData.getNetworkTable(nodeID)).orElseThrow();
  }

  public <T> MarginalTable getObservedTable(T nodeID) {
    if (!networkData.isSolved()) solveNetwork();
    return Optional.ofNullable(networkData.getObservedTable(nodeID)).orElseThrow();
  }
}
