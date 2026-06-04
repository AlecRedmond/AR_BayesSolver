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

/**
 * {@code InferenceEngine} provides utilities for running direct inference on a solved {@link
 * BayesianNetwork}. The values stored in the network's Conditional Probability Tables (CPTs) are
 * mapped to a secondary inference network which can be queried for joint, marginal, or conditional
 * probabilities. The documentation for {@link InferenceType} provides further information on the
 * available variations.
 *
 * <p>Specific {@link NodeState} values can be set as observed (always true) using {@link
 * #observeNetwork(Collection)} or {@link #observeNetworkFromIds(Collection)}. These will be
 * persisted in the instance until new observations are given, or until the observations are cleared
 * using {@link #resetObservations()}.
 *
 * <p>Unlike Monte Carlo sampling (as used in {@link Sampler}), the results of direct inference are
 * guaranteed to be correct.
 *
 * @see BayesSolver
 * @author Alec Redmond
 */
public interface InferenceEngine {

  /**
   * Creates an {@code InferenceEngine} from the given {@link BayesianNetwork}, using the default
   * inference variant configured in {@code app.properties}. The active variant is controlled by
   * {@code app.inference.useJunctionTreeInference} (default: {@code true}). This will automatically
   * solve the network if it is unsolved.
   *
   * @param network the network where inference is to be run.
   * @return a new {@code InferenceEngine} instance, or {@code null} if the given network was
   *     unsolved and could not be solved.
   */
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

  <T extends Serializable> MarginalTable getObservedTableById(T nodeId);

  MarginalTable getObservedTable(Node node);

  <T extends Serializable> MarginalTable copyObservedTableById(T nodeId);

  MarginalTable copyObservedTable(Node node);

  Map<Node, MarginalTable> getObservedTables();

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
