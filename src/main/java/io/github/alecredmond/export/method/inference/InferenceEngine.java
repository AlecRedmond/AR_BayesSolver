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

  InferenceEngine observeNetworkFromId(Serializable observedStateId);

  InferenceEngine observeNetworkFromIds(Collection<Serializable> observedStateIDs);

  MarginalTable getObservedTableById(Serializable nodeId);

  MarginalTable getObservedTable(Node node);

  MarginalTable copyObservedTableById(Serializable nodeId);

  MarginalTable copyObservedTable(Node node);

  Map<Node, MarginalTable> getObservedTables();

  double getCurrentProbability(Collection<NodeState> newEvidence);
}
