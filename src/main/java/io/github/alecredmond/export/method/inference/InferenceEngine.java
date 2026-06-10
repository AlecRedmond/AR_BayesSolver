package io.github.alecredmond.export.method.inference;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ObservedTable;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.inference.engine.InferenceEngineFactory;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface InferenceEngine {
  static InferenceEngine create(BayesianNetwork network) {
    return new InferenceEngineFactory().create(network);
  }

  static InferenceEngine create(BayesianNetwork network, InferenceType inferenceType) {
    return new InferenceEngineFactory().create(network, inferenceType);
  }

  InferenceEngine resetObservations();

  InferenceEngine observeNetwork(Collection<NodeState> observedStates);

  InferenceEngine observeNetwork(NodeState observedState);

  <T extends Serializable> InferenceEngine observeNetworkFromIds(T observedStateId);

  <T extends Serializable> InferenceEngine observeNetworkFromIds(Collection<T> observedStateIDs);

  Map<Node, NodeState> getCurrentObservations();

  <T extends Serializable> ObservedTable getObservedTableById(T nodeId);

  ObservedTable getObservedTable(Node node);

  <T extends Serializable> ObservedTable copyObservedTableById(T nodeId);

  ObservedTable copyObservedTable(Node node);

  Map<Node, ObservedTable> getObservedTables();

  double getCurrentProbability(Collection<NodeState> measuredStates);

  double getCurrentProbability(NodeState measuredState);

  <T extends Serializable> double getCurrentProbabilityById(Collection<T> measuredStateIds);

  <T extends Serializable> double getCurrentProbabilityById(T measuredStateId);

  InferenceEngine printObserved();

  <T extends Serializable> InferenceEngine printObservedById(Collection<T> nodeIds);

  <T extends Serializable> InferenceEngine printObservedById(T nodeId);

  InferenceEngine printObserved(Collection<Node> nodes);

  InferenceEngine printObserved(Node node);

  BayesianNetwork getNetwork();

  enum InferenceType {
    JOINT_TABLE_INFERENCE,
    JUNCTION_TREE_INFERENCE
  }
}
