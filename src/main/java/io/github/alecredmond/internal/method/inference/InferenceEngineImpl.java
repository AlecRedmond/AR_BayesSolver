package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.inference.junctiontree.JunctionTreeAlgorithm;
import io.github.alecredmond.internal.method.network.NetworkDataUtils;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.io.Serializable;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class InferenceEngineImpl implements InferenceEngine {
  private final BayesianNetwork network;
  private final JunctionTreeAlgorithm junctionTree;

  public InferenceEngineImpl(BayesianNetwork network, JunctionTreeAlgorithm junctionTree) {
    this.network = network;
    this.junctionTree = junctionTree;
  }

  @Override
  public InferenceEngineImpl resetObservations() {
    return observeNetwork(List.of());
  }

  @Override
  public InferenceEngineImpl observeNetwork(Collection<NodeState> observed) {
    if (!network.getNetworkData().isSolved()) {
      return this;
    }
    junctionTree.observeNetwork(NodeUtils.generateRequest(observed));
    junctionTree.writeObservations();
    return this;
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
    return junctionTree.getData().getObservedEvidence();
  }

  @Override
  public <T extends Serializable> MarginalTable getObservedTableById(T nodeId) {
    Node node = network.getNode(nodeId);
    return junctionTree.getData().getObservedTablesMap().get(node);
  }

  @Override
  public MarginalTable getObservedTable(Node node) {
    return junctionTree.getData().getObservedTablesMap().get(node);
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
    return junctionTree.getData().getObservedTablesMap();
  }

  @Override
  public double getCurrentConditionalProbability(Collection<NodeState> measuredStates) {
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
}
