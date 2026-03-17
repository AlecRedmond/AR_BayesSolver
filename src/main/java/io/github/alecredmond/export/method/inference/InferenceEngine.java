package io.github.alecredmond.export.method.inference;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.method.network.BayesianNetwork;
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

  InferenceEngine observeNetworkFromIds(Serializable observedStateId);

  InferenceEngine observeNetworkFromIds(Collection<Serializable> observedStateIDs);

  Map<Node, NodeState> getCurrentObservations();

  MarginalTable getObservedTableById(Serializable nodeId);

  MarginalTable getObservedTable(Node node);

  MarginalTable copyObservedTableById(Serializable nodeId);

  MarginalTable copyObservedTable(Node node);

  Map<Node, MarginalTable> getObservedTables();

  double getCurrentConditionalProbability(Collection<NodeState> measuredStates);

  double getCurrentConditionalProbabilityById(Collection<Serializable> measuredStateIds);
}
