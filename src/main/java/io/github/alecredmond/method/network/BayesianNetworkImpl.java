package io.github.alecredmond.method.network;

import io.github.alecredmond.application.inference.InferenceEngineConfigs;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.printer.PrinterConfigs;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.method.constraints.ConstraintBuilder;
import io.github.alecredmond.method.inference.InferenceEngine;
import io.github.alecredmond.method.printer.NetworkPrinter;
import java.util.*;
import lombok.Getter;

@Getter
class BayesianNetworkImpl implements BayesianNetwork {
  private final BayesianNetworkData networkData;
  private final InferenceEngineConfigs inferenceEngineConfigs;
  private final NetworkDataUtils utils;
  private final InferenceEngine inferenceEngine;
  private final PrinterConfigs printerConfigs;

  BayesianNetworkImpl(String networkName) {
    this.networkData = new BayesianNetworkData();
    networkData.setNetworkName(networkName);
    this.utils = new NetworkDataUtils(networkData);
    this.inferenceEngineConfigs = new InferenceEngineConfigs();
    this.inferenceEngine = new InferenceEngine(networkData, inferenceEngineConfigs);
    this.printerConfigs = new PrinterConfigs();
  }

  BayesianNetworkImpl() {
    this.networkData = new BayesianNetworkData();
    networkData.setNetworkName("UNNAMED_NETWORK");
    this.utils = new NetworkDataUtils(networkData);
    this.inferenceEngineConfigs = new InferenceEngineConfigs();
    this.inferenceEngine = new InferenceEngine(networkData, inferenceEngineConfigs);
    this.printerConfigs = new PrinterConfigs();
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
    utils.removeNode(node.getNodeID());
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
    networkData
        .getConstraints()
        .add(
            ConstraintBuilder.buildConstraint(
                eventStateID, conditionStateIDs, probability, networkData));
    return this;
  }

  public <T> BayesianNetworkImpl addConstraint(T eventStateID, double probability) {
    networkData.setSolved(false);
    networkData
        .getConstraints()
        .add(ConstraintBuilder.buildConstraint(eventStateID, probability, networkData));
    return this;
  }

  public BayesianNetworkImpl solverCyclesLimit(int cyclesLimit) {
    inferenceEngineConfigs.setSolverCyclesLimit(cyclesLimit);
    return this;
  }

  public BayesianNetworkImpl solverTimeLimit(int timeLimitSeconds) {
    inferenceEngineConfigs.setSolverTimeLimitSeconds(timeLimitSeconds);
    return this;
  }

  public BayesianNetworkImpl logIntervalSeconds(int seconds) {
    inferenceEngineConfigs.setSolverLogIntervalSeconds(seconds);
    return this;
  }

  public BayesianNetworkImpl solverConvergeThreshold(double threshold) {
    inferenceEngineConfigs.setSolverConvergeThreshold(threshold);
    return this;
  }

  public BayesianNetworkImpl solveNetwork() {
    utils.buildNetworkData();
    inferenceEngine.runSolver();
    observeMarginals();
    return this;
  }

  public BayesianNetworkImpl printObserved() {
    if (!networkData.isSolved()) solveNetwork();
    new NetworkPrinter(networkData, printerConfigs).printObserved();
    return this;
  }

  public BayesianNetworkImpl printNetwork() {
    if (!networkData.isSolved()) solveNetwork();
    new NetworkPrinter(networkData, printerConfigs).printNetwork();
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

  public BayesianNetworkData getNetworkData() {
    utils.buildNetworkData();
    return networkData;
  }

  public <T, E> List<List<T>> generateSamples(
      Collection<E> excludeNodeIDs,
      Collection<E> includeNodeIDs,
      int numberOfSamples,
      Class<T> tClass) {
    if (!networkData.isSolved()) solveNetwork();
    return inferenceEngine.generateSamples(
        utils.getNodesByID(excludeNodeIDs),
        utils.getNodesByID(includeNodeIDs),
        numberOfSamples,
        tClass);
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
