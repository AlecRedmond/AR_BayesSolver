package io.github.alecredmond.export.method.inference;

import io.github.alecredmond.exceptions.NetworkPrinterException;
import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ObservedTable;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.sampler.Sampler;
import io.github.alecredmond.internal.method.inference.engine.InferenceEngineFactory;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * {@code InferenceEngine} provides utilities for running direct inference on a solved {@link
 * BayesianNetwork}. The values stored in the network's Conditional Probability Tables (CPTs) are
 * mapped to a secondary inference network which can be queried for joint, marginal, or conditional
 * probabilities. The documentation for {@link InferenceType} provides further information on the
 * available variants.
 *
 * <p>Specific {@link NodeState} values can be set as observed (always true) using {@link
 * #observeNetwork(Collection)} or {@link #observeNetworkFromIds(Collection)}. These will be
 * persisted in the instance until new observations are given, or until the observations are cleared
 * using {@link #resetObservations()}.
 *
 * <p>Unlike Monte Carlo sampling (as used in {@link Sampler}), which will give an approximate
 * solution, the results of direct inference are both exact and deterministic.
 *
 * <p>Instances of this interface are not thread-safe. External synchronisation is required for
 * concurrent access.
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

  /**
   * Creates an {@code InferenceEngine} from a {@link BayesianNetwork} and a given {@link
   * InferenceType} variant. This will automatically solve the network if it is unsolved. Under
   * almost all circumstances the use of {@link InferenceType#JUNCTION_TREE_ALGORITHM} is advised.
   * Refer to the {@link InferenceType} documentation for further information.
   *
   * @param network the network where inference is to be run.
   * @param inferenceType the {@link InferenceType} variant to use for this instance.
   * @return a new {@code InferenceEngine} instance, or {@code null} if the given network was
   *     unsolved and could not be solved.
   */
  static InferenceEngine create(BayesianNetwork network, InferenceType inferenceType) {
    return new InferenceEngineFactory().create(network, inferenceType);
  }

  /**
   * Removes any observed states from the inference network, returning it to its unobserved
   * configuration. All measured probability values in this configuration will become prior
   * (unconditional) probabilities.
   *
   * @return this instance for chaining.
   */
  InferenceEngine resetObservations();

  /**
   * Replaces the current observed states in the inference network with the given states. Each
   * {@link NodeState} will lock its associated {@link Node} to that value. This will change all
   * measured probability values in this instance to posterior probabilities, conditional on these
   * states.
   *
   * @param observedStates the collection of states to be observed.
   * @return this instance for chaining.
   * @throws NodeStateConflictException if multiple {@link NodeState} values would map to the same
   *     {@link Node}.
   */
  InferenceEngine observeNetwork(Collection<NodeState> observedStates);

  /**
   * Replaces the current observed states in the inference network with the given {@link NodeState}.
   * This will lock its associated {@link Node} to that value. This will change all measured
   * probability values in this instance to posterior probabilities, conditional on this state.
   *
   * @param observedState the single state to be observed.
   * @return this instance for chaining.
   */
  InferenceEngine observeNetwork(NodeState observedState);

  /**
   * Replaces the current observed states in the inference network with the given {@link NodeState},
   * identified by its id. This will lock its associated {@link Node} to that value. This will
   * change all measured probability values in this instance to posterior probabilities, conditional
   * on this state.
   *
   * @param observedStateId the id of the single state to be observed.
   * @param <T> the type of the state id.
   * @return this instance for chaining.
   */
  <T extends Serializable> InferenceEngine observeNetworkFromIds(T observedStateId);

  /**
   * Replaces the current observed states in the inference network with the given states, identified
   * by their ids. Each {@link NodeState} will lock its associated {@link Node} to that value. This
   * will change all measured probability values in this instance to posterior probabilities,
   * conditional on these states.
   *
   * @param observedStateIDs the collection of ids associated with the states to be observed.
   * @param <T> the type of the state ids.
   * @return this instance for chaining.
   * @throws NodeStateConflictException if multiple {@link NodeState} values would map to the same
   *     {@link Node}.
   */
  <T extends Serializable> InferenceEngine observeNetworkFromIds(Collection<T> observedStateIDs);

  /**
   * Returns a map of each {@link Node} currently set as observed, and the specific {@link
   * NodeState} it is locked to.
   *
   * @return a map of the current observations on this instance.
   */
  Map<Node, NodeState> getCurrentObservations();

  /**
   * Returns an {@link ObservedTable} associated with a {@link Node}. This is a table containing
   * only the node's {@link NodeState} values, which are mapped to their current posterior
   * probability if this {@code InferenceEngine} has observations applied, or their prior (marginal)
   * probability otherwise. The values on this table are not deep-copied and will change if the
   * observations on this instance change.
   *
   * @param nodeId the id of the {@link Node} associated with the {@link ObservedTable}.
   * @param <T> the type of the node id.
   * @return a {@link ObservedTable} mapping a single node's states to their current probability.
   * @throws NullPointerException if the id was not associated with any {@link Node}.
   */
  <T extends Serializable> ObservedTable getObservedTableById(T nodeId);

  /**
   * Returns an {@link ObservedTable} associated with a {@link Node}. This is a table containing
   * only the node's {@link NodeState} values, which are mapped to their current posterior
   * probability if this {@code InferenceEngine} has observations applied, or their prior (marginal)
   * probability otherwise. The values on this table are not deep-copied and will change if the
   * observations on this instance change.
   *
   * @param node the {@link Node} associated with the {@link ObservedTable}.
   * @return a {@link ObservedTable} mapping a single node's states to their current probability.
   */
  ObservedTable getObservedTable(Node node);

  /**
   * Returns an {@link ObservedTable} associated with a {@link Node}. This is a table containing
   * only the node's {@link NodeState} values, which are mapped to their current posterior
   * probability if this {@code InferenceEngine} has observations applied, or their prior (marginal)
   * probability otherwise. This will provide a deep copy of the table, and the probability values
   * will not change if the observations on the instance change.
   *
   * @param nodeId the id of the {@link Node} associated with the {@link ObservedTable}.
   * @param <T> the type of the node id.
   * @return a {@link ObservedTable} mapping a single node's states to their probability given the
   *     conditions when constructed.
   * @throws NullPointerException if the id was not associated with any {@link Node}.
   */
  <T extends Serializable> ObservedTable copyObservedTableById(T nodeId);

  /**
   * Returns an {@link ObservedTable} associated with a {@link Node}. This is a table containing
   * only the node's {@link NodeState} values, which are mapped to their current posterior
   * probability if this {@code InferenceEngine} has observations applied, or their prior (marginal)
   * probability otherwise. This will provide a deep copy of the table, and the probability values
   * will not change if the observations on the instance change.
   *
   * @param node the {@link Node} associated with the {@link ObservedTable}.
   * @return a {@link ObservedTable} mapping a single node's states to their probability given the
   *     conditions when constructed.
   */
  ObservedTable copyObservedTable(Node node);

  /**
   * Returns a map of all {@link Node} objects present in the network, and their associated observed
   * {@link ObservedTable}. These tables contain only the node's {@link NodeState} values, which are
   * mapped to their current posterior probability if this {@code InferenceEngine} has observations
   * applied, or their prior (marginal) probability otherwise. These tables are not deep-copied and
   * will update when changes to the current observations are made.
   *
   * @return a map associating each {@link Node} with its observed probability table.
   */
  Map<Node, ObservedTable> getObservedTables();

  /**
   * Returns the posterior probability of the given {@link NodeState} values, or their prior
   * probability if this {@code InferenceEngine} is unobserved. This measures {@code P(S|O)} where S
   * is the intersection of all queried states and O is the current observations.
   *
   * @param measuredStates the states to be queried.
   * @return the posterior probability of all measured {@link NodeState} values.
   */
  double getPosteriorProbability(Collection<NodeState> measuredStates);

  /**
   * Returns the posterior probability of the given {@link NodeState} value, or its prior
   * probability if this {@code InferenceEngine} is unobserved. This measures {@code P(s|O)} where s
   * is the queried state and O is the current observations.
   *
   * @param measuredState the state to be queried.
   * @return the posterior probability of the measured {@link NodeState} value.
   */
  double getPosteriorProbability(NodeState measuredState);

  /**
   * Returns the posterior probability of the given {@link NodeState} values, or their prior
   * probability if this {@code InferenceEngine} is unobserved. This measures {@code P(S|O)} where S
   * is the intersection of all queried states and O is the current observations.
   *
   * @param measuredStateIds the ids of all {@link NodeState} values to be queried.
   * @param <T> the type of the measured {@link NodeState} ids.
   * @return the posterior probability of all measured {@link NodeState} values.
   * @throws NullPointerException if any id was not associated with a {@link NodeState}.
   */
  <T extends Serializable> double getPosteriorProbabilityById(Collection<T> measuredStateIds);

  /**
   * Returns the posterior probability of the given {@link NodeState} value, or its prior
   * probability if this {@code InferenceEngine} is unobserved. This measures {@code P(s|O)} where s
   * is the queried state and O is the current observations.
   *
   * @param measuredStateId the id of the {@link NodeState} to be queried.
   * @param <T> the type of the measured {@link NodeState} id.
   * @return the posterior probability of the measured {@link NodeState} value.
   * @throws NullPointerException if the id was not associated with any {@link NodeState}.
   */
  <T extends Serializable> double getPosteriorProbabilityById(T measuredStateId);

  /**
   * Prints the posterior probability values from all {@link ObservedTable} entries, either to a
   * {@code .txt} file or to the console. Parameters for the printer can be defined within {@code
   * app.properties} under the {@code app.printer} section.
   *
   * @return this instance for chaining.
   * @throws NetworkPrinterException if the printer is unable to successfully complete the
   *     operation.
   */
  InferenceEngine printObserved();

  /**
   * Prints the posterior probability values from the {@link ObservedTable} entries associated with
   * the given {@link Node}, either to a {@code .txt} file or to the console. Parameters for the
   * printer can be defined within {@code app.properties} under the {@code app.printer} section.
   *
   * @param nodeIds the ids of all {@link Node} values where the associated {@link ObservedTable}
   *     should be printed.
   * @param <T> the type of the {@link Node} ids.
   * @return this instance for chaining.
   * @throws NetworkPrinterException if the printer is unable to successfully complete the
   *     operation.
   * @throws NullPointerException if any ids were not associated with a {@link Node}.
   */
  <T extends Serializable> InferenceEngine printObservedById(Collection<T> nodeIds);

  /**
   * Prints the posterior probability values from the {@link ObservedTable} entries associated with
   * the given {@link Node}, either to a {@code .txt} file or to the console. Parameters for the
   * printer can be defined within {@code app.properties} under the {@code app.printer} section.
   *
   * @param nodeId the id of the {@link Node} where the associated {@link ObservedTable} should be
   *     printed.
   * @param <T> the class of the {@link Node} id.
   * @return this instance for chaining.
   * @throws NetworkPrinterException if the printer is unable to successfully complete the
   *     operation.
   * @throws NullPointerException if the id was not associated with any {@link Node}.
   */
  <T extends Serializable> InferenceEngine printObservedById(T nodeId);

  /**
   * Prints the posterior probability values from the {@link ObservedTable} entries associated with
   * the given {@link Node}, either to a {@code .txt} file or to the console. Parameters for the
   * printer can be defined within {@code app.properties} under the {@code app.printer} section.
   *
   * @param nodes all {@link Node} values where the associated {@link ObservedTable} should be
   *     printed.
   * @return this instance for chaining.
   * @throws NetworkPrinterException if the printer is unable to successfully complete the
   *     operation.
   */
  InferenceEngine printObserved(Collection<Node> nodes);

  /**
   * Prints the posterior probability values from the {@link ObservedTable} entries associated with
   * the given {@link Node}, either to a {@code .txt} file or to the console. Parameters for the
   * printer can be defined within {@code app.properties} under the {@code app.printer} section.
   *
   * @param node a {@link Node} where the associated {@link ObservedTable} should be printed.
   * @return this instance for chaining.
   * @throws NetworkPrinterException if the printer is unable to successfully complete the
   *     operation.
   */
  InferenceEngine printObserved(Node node);

  /**
   * Returns the {@link BayesianNetwork} measured by this {@code InferenceEngine}.
   *
   * @return the {@link BayesianNetwork} associated with this {@code InferenceEngine}.
   */
  BayesianNetwork getNetwork();

  /**
   * The variants of direct inference an {@code InferenceEngine} can perform. These are set on the
   * creation of an {@code InferenceEngine} instance and cannot be changed. There are currently two
   * variants of inference that can be used, and the default can be configured in {@code
   * app.properties} using {@code app.inference.useJunctionTreeInference} (default: {@code true}).
   *
   * <ul>
   *   <li><b>JUNCTION_TREE_ALGORITHM</b> (JTA) is the default inference algorithm and its use is
   *       advisable in almost all cases. JTA decomposes the network into a junction tree of smaller
   *       cliques, each with its own joint sub-table, connected by separators formed from their
   *       shared nodes. Observations are applied per-clique and propagated via message-passing
   *       through the separators. JTA scales on the treewidth of the network, and will typically
   *       yield an exponential improvement over the joint table method at the cost of a small
   *       overhead. It is also the only option for networks whose full joint Cartesian product
   *       would exceed 2<sup>31</sup>&minus;1 entries.
   *   <li><b>SINGLE_TABLE_ALGORITHM</b> (STA) is generally not advisable. STA maps the Cartesian
   *       product of all node states in the network to a single joint probability table. This has a
   *       low overhead but very poor time and memory complexity scaling, although there may be some
   *       benefit in very small networks under high loads of observation queries. The absolute
   *       upper limit for this variant is a table length of 2<sup>31</sup>&minus;1 entries, the
   *       maximum length of a Java array.
   * </ul>
   */
  enum InferenceType {
    /**
     * <b>JUNCTION_TREE_ALGORITHM</b> (JTA) is the default inference algorithm and its use is
     * advisable in almost all cases. JTA decomposes the network into a junction tree of smaller
     * cliques, each with its own joint sub-table, connected by separators formed from their shared
     * nodes. Observations are applied per-clique and propagated via message-passing through the
     * separators. JTA scales on the treewidth of the network, and will typically yield an
     * exponential time and memory complexity improvement over the joint table method at the cost of
     * a small overhead. It is also the only option for networks whose full joint Cartesian product
     * would exceed 2<sup>31</sup>&minus;1 entries.
     */
    JUNCTION_TREE_ALGORITHM,
    /**
     * <b>SINGLE_TABLE_ALGORITHM</b> (STA) is generally not advisable for. STA maps the Cartesian
     * product of all node states in the network to a single joint probability table. This has a low
     * overhead but very poor time and memory complexity scaling, although there may be some benefit
     * in very small networks under high loads of observation queries. The absolute upper limit for
     * this variant is a table length of 2<sup>31</sup>&minus;1 entries, the maximum length of a
     * Java array.
     */
    SINGLE_TABLE_ALGORITHM
  }
}
