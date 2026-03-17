package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.method.inference.InferenceEngine;
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
  private final BayesianNetworkData networkData;
  private final JunctionTreeAlgorithm junctionTree;

  public InferenceEngineImpl(BayesianNetworkData networkData, JunctionTreeAlgorithm junctionTree) {
    this.networkData = networkData;
    this.junctionTree = junctionTree;
  }

  @Override
  public InferenceEngineImpl resetObservations() {
    return observeNetwork(List.of());
  }

  @Override
  public InferenceEngineImpl observeNetwork(Collection<NodeState> observed) {
    if (!networkData.isSolved()) {
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
  public InferenceEngine observeNetworkFromId(Serializable observedStateId) {
    return observeNetworkFromIds(List.of(observedStateId));
  }

  @Override
  public InferenceEngine observeNetworkFromIds(Collection<Serializable> observedStateIDs) {
    return observeNetwork(NetworkDataUtils.getStatesByID(observedStateIDs, networkData));
  }

  @Override
  public MarginalTable getObservedTableById(Serializable nodeId) {
    Node node = networkData.getNodeIDsMap().get(nodeId);
    return junctionTree.getData().getObservedTablesMap().get(node);
  }

  @Override
  public MarginalTable getObservedTable(Node node) {
    return junctionTree.getData().getObservedTablesMap().get(node);
  }

  @Override
  public MarginalTable copyObservedTableById(Serializable nodeId) {
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
  public double getCurrentProbability(Collection<NodeState> newEvidence) {
    try {
      double currentJointProb = junctionTree.getJointProbability();
      if (currentJointProb == 0.0) return 0.0;
      double newJointProb = junctionTree.getJointProbOfNewEvidence(newEvidence);
      return newJointProb / currentJointProb;
    } catch (NodeStateConflictException e) {
      log.warn("Conflicting states found in {}", NodeUtils.formatStatesToString(newEvidence));
      return 0.0;
    }
  }
}
