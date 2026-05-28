package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.method.inference.BayesSolver;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.sampler.Sampler;
import io.github.alecredmond.internal.method.inference.junctiontree.JunctionTreeAlgorithm;
import io.github.alecredmond.internal.method.network.NetworkDataUtils;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.printer.NetworkPrinter;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class InferenceEngineImpl implements InferenceEngine {
  private final BayesianNetwork network;
  private final BayesSolver solver;
  private final JunctionTreeAlgorithm junctionTree;
  private final InferenceType inferenceType;

  public InferenceEngineImpl(
      BayesianNetwork network, JunctionTreeAlgorithm junctionTree, InferenceType inferenceType) {
    this.network = network;
    this.junctionTree = junctionTree;
    this.solver = BayesSolver.create(network);
    this.inferenceType = inferenceType;
    resetObservations();
  }

  @Override
  public InferenceEngineImpl resetObservations() {
    return observeNetwork(List.of());
  }

  @Override
  public InferenceEngineImpl observeNetwork(Collection<NodeState> observed) {
    if (!ensureSolved()) {
      return this;
    }
    junctionTree.observeNetwork(NodeUtils.generateRequest(observed));
    junctionTree.writeObservations();
    return this;
  }

  private boolean ensureSolved() {
    if (solver.isSolved()) {
      return true;
    }
    log.info(
        "Modifications were detected on network {}, solver will be re-run",
        network.getNetworkData().getNetworkName());
    if (solver.solve()) {
      junctionTree.rebuildJTA(network.getNetworkData(), inferenceType);
      return true;
    }
    log.error("Could not solve network {}!", network.getNetworkData().getNetworkName());
    return false;
  }

  @Override
  public InferenceEngine observeNetwork(NodeState observedState) {
    return observeNetwork(List.of(observedState));
  }

  @Override
  public <T extends Serializable> InferenceEngine observeNetworkFromIds(T observedStateId) {
    return observeNetworkFromIds(List.of(observedStateId));
  }

  @Override
  public <T extends Serializable> InferenceEngine observeNetworkFromIds(
      Collection<T> observedStateIDs) {
    return observeNetwork(
        NetworkDataUtils.getStatesByID(observedStateIDs, network.getNetworkData()));
  }

  @Override
  public Map<Node, NodeState> getCurrentObservations() {
    return ensureSolved() ? junctionTree.getData().getObservedEvidence() : new HashMap<>();
  }

  @Override
  public <T extends Serializable> MarginalTable getObservedTableById(T nodeId) {
    Node node = network.getNode(nodeId);
    return junctionTree.getData().getObservedTablesMap().get(node);
  }

  @Override
  public MarginalTable getObservedTable(Node node) {
    return ensureSolved() ? junctionTree.getData().getObservedTablesMap().get(node) : null;
  }

  @Override
  public <T extends Serializable> MarginalTable copyObservedTableById(T nodeId) {
    return getObservedTableById(nodeId).getHelper().copyTable();
  }

  @Override
  public MarginalTable copyObservedTable(Node node) {
    return getObservedTable(node).getHelper().copyTable();
  }

  @Override
  public Map<Node, MarginalTable> getObservedTables() {
    return ensureSolved() ? junctionTree.getData().getObservedTablesMap() : new HashMap<>();
  }

  @Override
  public double getCurrentProbability(Collection<NodeState> measuredStates) {
    if (!ensureSolved()) {
      return 0.0;
    }
    double currentJointProb = junctionTree.getJointProbability();
    if (currentJointProb == 0.0) return 0.0;
    double newJointProb = junctionTree.getJointProbOfMeasured(measuredStates);
    return newJointProb / currentJointProb;
  }

  @Override
  public <T extends Serializable> double getCurrentProbabilityById(Collection<T> measuredStateIds) {
    return getCurrentProbability(
        NetworkDataUtils.getStatesByID(measuredStateIds, network.getNetworkData()));
  }

  @Override
  public InferenceEngine printObserved() {
    if (!ensureSolved()) return this;
    new NetworkPrinter(this).printObserved();
    return this;
  }

  @Override
  public InferenceEngine printObservedById(Collection<Serializable> nodeIds) {
    return printObserved(network.getNodes(nodeIds));
  }

  @Override
  public InferenceEngine printObserved(Collection<Node> nodes) {
    if (!ensureSolved()) return this;
    Set<Node> nodeSet = new HashSet<>(nodes);
    Map<Node, MarginalTable> toPrint =
        junctionTree.getData().getObservedTablesMap().entrySet().stream()
            .filter(e -> nodeSet.contains(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    new NetworkPrinter(this).printTables(toPrint, "OBSERVED");
    return this;
  }

  @Override
  public Sampler createSampler() {
    return Sampler.create(this);
  }
}
