package io.github.alecredmond.export.method.inference;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.sampler.Sampler;
import io.github.alecredmond.internal.method.inference.InferenceEngineFactory;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface InferenceEngine {
  static InferenceEngine create(BayesianNetwork network) {
    return new InferenceEngineFactory().create(network);
  }

  InferenceEngine resetObservations();

  InferenceEngine observeNetwork(Collection<NodeState> observedStates);

  InferenceEngine observeNetwork(NodeState observedState);

  <T extends Serializable> InferenceEngine observeNetworkFromIds(T observedStateId);

  <T extends Serializable> InferenceEngine observeNetworkFromIds(Collection<T> observedStateIDs);

  Map<Node, NodeState> getCurrentObservations();

  <T extends Serializable> MarginalTable getObservedTableById(T nodeId);

  MarginalTable getObservedTable(Node node);

  <T extends Serializable> MarginalTable copyObservedTableById(T nodeId);

  MarginalTable copyObservedTable(Node node);

  Map<Node, MarginalTable> getObservedTables();

  double getCurrentConditionalProbability(Collection<NodeState> measuredStates);

  <T extends Serializable> double getCurrentConditionalProbabilityById(
      Collection<T> measuredStateIds);

  InferenceEngine printObserved();

  BayesianNetwork getNetwork();

  Sampler createSampler();
}
