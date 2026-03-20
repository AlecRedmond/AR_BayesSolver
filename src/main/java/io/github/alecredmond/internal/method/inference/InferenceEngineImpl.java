package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.exceptions.NodeStateConflictException;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class InferenceEngineImpl implements InferenceEngine {
  private final BayesianNetwork network;
  private final BayesSolver solver;
  private final JunctionTreeAlgorithm junctionTree;

  public InferenceEngineImpl(BayesianNetwork network, JunctionTreeAlgorithm junctionTree) {
    this.network = network;
    this.junctionTree = junctionTree;
    this.solver = BayesSolver.create(network);
    observeMarginals();
  }

  @Override
  public InferenceEngineImpl observeMarginals() {
    return observeNetwork(List.of());
  }

  @Override
  public InferenceEngineImpl observeNetwork(Collection<NodeState> observed) {
    if (!checkSolved()) {
      return this;
    }
    junctionTree.observeNetwork(NodeUtils.generateRequest(observed));
    junctionTree.writeObservations();
    return this;
  }

  private boolean checkSolved() {
    if (solver.isSolved()) {
      return true;
    }
    log.info(
        "Modifications were detected on network {}, solver will be re-run",
        network.getNetworkData().getNetworkName());
    if (solver.solve()) {
      junctionTree.rebuildJTA(network.getNetworkData());
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
    return checkSolved() ? junctionTree.getData().getObservedEvidence() : new HashMap<>();
  }

  @Override
  public <T extends Serializable> MarginalTable getObservedTableById(T nodeId) {
    Node node = network.getNode(nodeId);
    return junctionTree.getData().getObservedTablesMap().get(node);
  }

  @Override
  public MarginalTable getObservedTable(Node node) {
    return checkSolved() ? junctionTree.getData().getObservedTablesMap().get(node) : null;
  }

  @Override
  public <T extends Serializable> MarginalTable copyObservedTableById(T nodeId) {
    return getObservedTableById(nodeId).copyTable();
  }

  @Override
  public MarginalTable copyObservedTable(Node node) {
    return getObservedTable(node).copyTable();
  }

  @Override
  public Map<Node, MarginalTable> getObservedTables() {
    return checkSolved() ? junctionTree.getData().getObservedTablesMap() : new HashMap<>();
  }

  @Override
  public double getCurrentConditionalProbability(Collection<NodeState> measuredStates) {
    if (!checkSolved()) {
      return 0.0;
    }
    try {
      double currentJointProb = junctionTree.getJointProbability();
      if (currentJointProb == 0.0) return 0.0;
      double newJointProb = junctionTree.getJointProbOfMeasured(measuredStates);
      return newJointProb / currentJointProb;
    } catch (NodeStateConflictException e) {
      log.warn("Conflicting states found in {}", NodeUtils.formatStatesToString(measuredStates));
      return 0.0;
    }
  }

  @Override
  public <T extends Serializable> double getCurrentConditionalProbabilityById(
      Collection<T> measuredStateIds) {
    return getCurrentConditionalProbability(
        NetworkDataUtils.getStatesByID(measuredStateIds, network.getNetworkData()));
  }

  @Override
  public InferenceEngine printObserved() {
    if (!checkSolved()) return this;
    new NetworkPrinter(this).printObserved();
    return this;
  }

  @Override
  public Sampler createSampler() {
    return Sampler.create(this);
  }
}
