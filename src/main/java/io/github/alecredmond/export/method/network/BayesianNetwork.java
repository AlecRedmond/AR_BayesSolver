package io.github.alecredmond.export.method.network;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.method.inference.BayesSolver;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.inference.InferenceEngine.InferenceType;
import io.github.alecredmond.export.method.sampler.Sampler;
import io.github.alecredmond.export.serialization.network.SerializedBayesianNetwork;
import io.github.alecredmond.internal.fileio.NetworkFileIO;
import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;
import io.github.alecredmond.internal.serialization.BayesianNetworkSerializer;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * An interface that provides a toolset for building, solving, and providing direct inference on a
 * Bayesian Network.
 *
 * <p>Marginal and Conditional probability values are not directly entered into the network, but as
 * <i>constraints</i> on the network. An Iterative Proportional Fitting Procedure (IPFP) is then
 * carried out to find a 'best fit' for the network tables based on the given constraints.
 *
 * <p>It is worth noting that for each node on the network linked by a constraint outside its scope
 * (i.e. not linked to a parent or child node), another virtual edge is created on the network's
 * graph. If enough additional edges are created, the solver will not subdivide the graph into
 * cliques, and the algorithm becomes no more efficient than standard IPFP. This does not affect
 * subsequent sampling as a new JTA instance is created upon completion of the solver's run.
 *
 * @author Alec Redmond
 * @version 1.0.0 RELEASE
 */
@SuppressWarnings("unused")
public interface BayesianNetwork {

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRUCTORS------------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Creates a new, empty Bayesian Network with the default name "UNNAMED NETWORK".
   *
   * @return a new {@code BayesianNetwork} instance.
   */
  static BayesianNetwork newNetwork() {
    return new BayesianNetworkImpl();
  }

  /**
   * Creates a new, empty Bayesian Network with a specified name.
   *
   * @param networkName the name for the new network.
   * @return a new {@code BayesianNetwork} instance.
   */
  static BayesianNetwork newNetwork(String networkName) {
    return new BayesianNetworkImpl(networkName);
  }

  /**
   * Loads a saved {@code BayesianNetwork} from a JFileChooser window
   *
   * @return a loaded {@code BayesianNetwork} instance
   */
  static BayesianNetwork loadNetworkFromFile() {
    return new NetworkFileIO(new BayesianNetworkSerializer()).loadNetwork();
  }

  /**
   * Loads a saved {@code BayesianNetwork}.
   *
   * @param file the selected .bayes file to load
   * @return a loaded BayesianNetwork instance
   */
  static BayesianNetwork loadNetworkFromFile(File file) {
    return new NetworkFileIO(new BayesianNetworkSerializer()).loadNetwork(file);
  }

  /**
   * Loads a saved {@code BayesianNetwork}.
   *
   * @param filePath the absolute path to a .bayes file
   * @return a loaded BayesianNetwork instance
   */
  static BayesianNetwork loadNetworkFromFile(String filePath) {
    return new NetworkFileIO(new BayesianNetworkSerializer()).loadNetwork(filePath);
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NETWORK FILE IO---------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Saves the network to the disk
   *
   * @param file the new file
   * @return <code>true</code> if the save operation was successful
   */
  boolean saveNetworkToFile(File file);

  /**
   * Saves the network to the disk
   *
   * @param filePath path to the new file
   * @return <code>true</code> if the save operation was successful
   */
  boolean saveNetworkToFile(String filePath);

  /**
   * Saves the network to the disk from a JFileChooser window.
   *
   * @return <code>true</code> if the save operation was successful
   */
  boolean saveNetworkToFile();

  /**
   * Returns a new serialized Bayesian network for IO operations
   *
   * @return a serialization of the current bayesian network data
   */
  SerializedBayesianNetwork serializeNetwork();

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NODE IN/OUT-------------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Adds a node to the network. The node will be associated with a Conditional Probability Table
   * (CPT) of the form P(N|Parents(N)).
   *
   * @param node the node to be added to the network
   * @throws BayesNetIDException if the nodeID is not unique
   * @return this instance for method chaining.
   */
  BayesianNetwork addNode(Node node);

  /**
   * Adds a node to the network. The node will be associated with a Conditional Probability Table
   * (CPT) of the form P(N|Parents(N)).
   *
   * @param nodeID the unique identifier for the node.
   * @param <T> the class of the node ID
   * @throws BayesNetIDException if the nodeID is not unique
   * @return this instance for method chaining.
   */
  <T extends Serializable> BayesianNetwork addNewNode(T nodeID);

  /**
   * Adds a node (N) with a specified set of possible states. The states represent the mutually
   * exclusive values the node's variable can take, e.g., {N:TRUE, N:FALSE} or {N:LOW, N:MEDIUM,
   * N:HIGH}. If using descriptive IDs, it is highly recommended to pre-append the nodeID to each
   * nodeStateID, as each nodeState in the network requires a unique identifier.
   *
   * @param nodeID the unique identifier for the node.
   * @param nodeStateIDs a collection of unique identifiers for each state.
   * @param <T> the class of the Node ID
   * @param <E> the class of the NodeState IDs
   * @throws BayesNetIDException if the nodeID or each nodeStateID is not unique
   * @return this instance for method chaining.
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork addNewNode(
      T nodeID, Collection<E> nodeStateIDs);

  /**
   * Removes a node and all associated edges from the network.
   *
   * @param node the node to remove.
   * @return true if the network contained the specified Node.
   */
  boolean removeNode(Node node);

  /**
   * Removes a node and all associated edges from the network.
   *
   * @param nodeID the identifier of the node to remove.
   * @param <T> the class of the Node ID
   * @return true if the network contained the specified Node
   */
  <T extends Serializable> boolean removeNodeByID(T nodeID);

  /**
   * Removes all nodes from the network, resetting it to an empty state.
   *
   * @return true if the network contained any nodes.
   */
  boolean removeAllNodes();

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NODE/STATE GETTERS------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Returns a node from its input ID
   *
   * @param <T> class of the Node ID
   * @param nodeID the node ID
   * @throws IllegalArgumentException if the node ID is not mapped to a node value
   * @return the Node object associated with the ID
   */
  <T extends Serializable> Node getNode(T nodeID);

  /**
   * Returns a set of nodes from their input ID
   *
   * @param <T> class of the Node ID
   * @param nodeIDs the node IDs
   * @throws IllegalArgumentException if the node IDs are not mapped to a node value
   * @return the Node object associated with the ID
   */
  <T extends Serializable> Set<Node> getNodes(Collection<T> nodeIDs);

  /**
   * Returns a Node State from its input ID
   *
   * @param <E> class of the Node State ID
   * @param nodeStateID the Node State ID
   * @return the Node State object associated with the ID
   */
  <E extends Serializable> NodeState getNodeState(E nodeStateID);

  /**
   * Returns a set of NodeStates from their input IDs
   *
   * @param <E> class of the Node State IDs
   * @param nodeStateIDs the Node State IDs
   * @return a set of Node State objects associated with their IDs
   */
  <E extends Serializable> Set<NodeState> getNodeStates(Collection<E> nodeStateIDs);

  // ----------------------------------------------------------------------------------------------
  // ---------------------------------NODE PARENT/CHILD RELATIONS----------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Defines parent-child relationships by adding directed edges from parent nodes to a child node.
   *
   * @param child the child node.
   * @param parents a collection of parent nodes.
   * @throws NetworkStructureException if the node would parent itself or cause a cycle in the graph
   * @return this instance for method chaining.
   */
  BayesianNetwork addParents(Node child, Collection<Node> parents);

  /**
   * Defines parent-child relationships by adding directed edges from parent nodes to a child node.
   *
   * @param childID the identifier of the child node.
   * @param parentIDs a collection of identifiers for the parent nodes.
   * @throws NetworkStructureException if the node would parent itself or cause a cycle in the graph
   * @return this instance for method chaining.
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork addParents(
      T childID, Collection<E> parentIDs);

  /**
   * Defines a parent-child relationship by adding a directed edge from a parent node to a child
   * node.
   *
   * @param child the child node.
   * @param parent the parent node
   * @throws NetworkStructureException if the node would parent itself or cause a cycle in the graph
   * @return this instance for method chaining.
   */
  BayesianNetwork addParents(Node child, Node parent);

  /**
   * Defines a parent-child relationship by adding a directed edge from a parent node to a child
   * node.
   *
   * @param childID the identifier of the child node.
   * @param parentID the identifier of the parent node.
   * @param <T> the class of the Child Node ID
   * @param <E> the class of the Parent Node ID
   * @throws NetworkStructureException if the node would parent itself or cause a cycle in the graph
   * @return this instance for method chaining.
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork addParents(
      T childID, E parentID);

  /**
   * Removes a directed edge between a parent and a child node.
   *
   * @param child the child node.
   * @param parent the node to remove.
   * @return this instance for method chaining.
   */
  BayesianNetwork removeParent(Node child, Node parent);

  /**
   * Removes a directed edge between a parent and a child node.
   *
   * @param childID the identifier of the child node.
   * @param parentID the identifier of the parent node to remove.
   * @param <T> the class of the Child Node ID
   * @param <E> the class of the Parent Node ID
   * @return this instance for method chaining.
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork removeParent(
      T childID, E parentID);

  /**
   * Removes all parent relationships for a given child node, removing all incoming edges from the
   * child node.
   *
   * @param child the child node whose parents will be removed.
   * @return this instance for method chaining.
   */
  BayesianNetwork removeParents(Node child);

  /**
   * Removes all parent relationships for a given child node, removing all incoming edges from the
   * child node.
   *
   * @param childID the identifier of the child node whose parents will be removed.
   * @param <T> the class of the Child Node ID
   * @return this instance for method chaining.
   */
  <T extends Serializable> BayesianNetwork removeParents(T childID);

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRAINT ADDERS-------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Adds a marginal probability constraint on the network, P(event) = probability. This can be
   * either a prior probability on a node which has no parents, or a known marginal outcome on a
   * child node.
   *
   * @param eventStateID the state of the root node.
   * @param probability the prior probability value.
   * @param <T> the class of the event state ID
   * @throws IllegalArgumentException if the state is not found within the data.
   * @throws ConstraintValidationException <br>
   *     - if probability p is outwith 0 <= p <= 1 <br>
   *     - if the state is not found within the data.
   * @return this instance for method chaining.
   */
  <T extends Serializable> BayesianNetwork addConstraint(T eventStateID, double probability);

  /**
   * Adds a conditional probability constraint to the network: P(event | conditions) = probability.
   * This constraint doesn't have to be within the scope of the network's structure, but each
   * conditional constraint will add another virtual "edge" to the graph during the solving process.
   * This may prevent the Junction Tree solver from decomposing the graph into cliques, potentially
   * increasing the time complexity from its base of {@code O(2^Max(Parents/Node))} up to a maximum
   * of {@code O(2^Nodes)}.
   *
   * @param eventStateID the id of the event NodeState.
   * @param conditionStateId the id Condition NodeState.
   * @param probability the conditional probability value.
   * @param <T> the class of the event state ID
   * @param <E> the class of the condition state IDs
   * @throws ConstraintValidationException <br>
   *     - if attempting to make a state conditional on another state from the same node <br>
   *     - if probability p is outwith 0 <= p <= 1
   * @return this instance for method chaining.
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork addConstraint(
      T eventStateID, E conditionStateId, double probability);

  /**
   * Adds a conditional probability constraint to the network: P(event | conditions) = probability.
   * This constraint doesn't have to be within the scope of the network's structure, but each
   * conditional constraint will add another virtual "edge" to the graph during the solving process.
   * This may prevent the Junction Tree solver from decomposing the graph into cliques, potentially
   * increasing the time complexity from its base of {@code O(2^Max(Parents/Node))} up to a maximum
   * of {@code O(2^Nodes)}.
   *
   * @param eventStateID the state of the child node.
   * @param conditionStateIDs the combination of parent states.
   * @param probability the conditional probability value.
   * @param <T> the class of the event state ID
   * @param <E> the class of the condition state IDs
   * @throws ConstraintValidationException <br>
   *     - if attempting to make a state conditional on another state from the same node <br>
   *     - if probability p is outwith 0 <= p <= 1
   * @return this instance for method chaining.
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork addConstraint(
      T eventStateID, Collection<E> conditionStateIDs, double probability);

  <T extends Serializable, E extends Serializable> BayesianNetwork addConstraint(
      Collection<T> eventStateIDs, Collection<E> conditionStateIDs, double probability);

  /**
   * Adds a probability constraint to the network This constraint doesn't have to be within the
   * scope of the network's structure, but each conditional constraint will add another virtual
   * "edge" to the graph during the solving process. This may prevent the Junction Tree solver from
   * decomposing the graph into cliques, potentially increasing the time complexity from its base of
   * {@code O(2^Max(Parents/Node))} up to a maximum of {@code O(2^Nodes)}.
   *
   * @param probabilityConstraint a ProbabilityConstraint object such as {@link MarginalConstraint}
   *     or {@link ConditionalConstraint}
   * @throws ConstraintValidationException <br>
   *     - if probability p is outwith 0 <= p <= 1 <br>
   *     - if attempting to make a state conditional on another state from the same node <br>
   *     - if attempting to add a MarginalConstraint with non-empty conditions
   * @return this instance for method chaining
   */
  BayesianNetwork addConstraint(ProbabilityConstraint probabilityConstraint);

  /**
   * Adds a collection of {@link ProbabilityConstraint}s to this {@code BayesianNetwork}.
   *
   * @param probabilityConstraints a collection of {@link ProbabilityConstraint}s to be added.
   * @return this instance for method chaining.
   * @throws ConstraintValidationException if any {@link ProbabilityConstraint} is in an illegal
   *     configuration.
   */
  BayesianNetwork addConstraints(Collection<ProbabilityConstraint> probabilityConstraints);

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRAINT GETTERS------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Searches this {@code BayesianNetwork} for a {@link MarginalConstraint} with an event {@link
   * NodeState} matching the given identifier.
   *
   * @param eventStateId the identifier of a {@link NodeState}.
   * @param <T> the class of the event {@link NodeState} identifier.
   * @return the associated {@link MarginalConstraint}, or {@code null} if it does not exist.
   * @throws NullPointerException if the parameter is {@code null}.
   */
  <T extends Serializable> MarginalConstraint getConstraint(T eventStateId);

  /**
   * Searches this {@code BayesianNetwork} for a {@link ProbabilityConstraint} with event and
   * condition {@link NodeState}s matching the given identifiers.
   *
   * @param eventStateId the identifier of the event {@link NodeState}.
   * @param conditionStateIds the identifiers of the condition {@link NodeState}s.
   * @param <T> the class of the event {@link NodeState} identifier.
   * @param <E> the class of the condition {@link NodeState} identifiers.
   * @return the associated {@link ProbabilityConstraint}, or {@code null} if it does not exist.
   * @throws NullPointerException if any parameter is {@code null}.
   */
  <T extends Serializable, E extends Serializable> ProbabilityConstraint getConstraint(
      T eventStateId, Collection<E> conditionStateIds);

  /**
   * Searches this {@code BayesianNetwork} for a {@link ProbabilityConstraint} with event and
   * condition {@link NodeState}s matching the given identifiers.
   *
   * @param eventStateIds the identifiers of the event {@link NodeState}s.
   * @param conditionStateIds the identifiers of the condition {@link NodeState}s.
   * @param <T> the class of the event {@link NodeState} identifiers.
   * @param <E> the class of the condition {@link NodeState} identifiers.
   * @return the associated {@link ProbabilityConstraint}, or {@code null} if it does not exist.
   * @throws NullPointerException if any parameter is {@code null}.
   */
  <T extends Serializable, E extends Serializable> ProbabilityConstraint getConstraint(
      Collection<T> eventStateIds, Collection<E> conditionStateIds);

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRAINT REMOVERS-----------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Removes a {@link ProbabilityConstraint} from this {@code BayesianNetwork}.
   *
   * @param probabilityConstraint the {@link ProbabilityConstraint} to remove.
   * @return {@code true} if the {@link ProbabilityConstraint} existed in the network.
   */
  boolean removeConstraint(ProbabilityConstraint probabilityConstraint);

  /**
   * Removes a {@link MarginalConstraint} matching the given event {@link NodeState}'s identifier
   * from this {@code BayesianNetwork}.
   *
   * @param eventStateId the identifier of the event {@link NodeState} in the {@link
   *     MarginalConstraint}.
   * @param <T> the class of the event {@link NodeState} identifier.
   * @return {@code true} if a matching {@link MarginalConstraint} existed in the network
   */
  <T extends Serializable> boolean removeConstraint(T eventStateId);

  /**
   * Removes a {@link ProbabilityConstraint} matching the given inputs from this {@code
   * BayesianNetwork}.
   *
   * @param eventStateId the identifier of the event {@link NodeState} in the {@link
   *     ProbabilityConstraint}.
   * @param conditionStateIds the collected ids of all condition {@link NodeState}s in the {@link
   *     ProbabilityConstraint}.
   * @param <T> the class of the event {@link NodeState} identifier.
   * @param <E> the class of the condition {@link NodeState} identifiers.
   * @return {@code true} if a matching {@link ProbabilityConstraint} existed in the network
   */
  <T extends Serializable, E extends Serializable> boolean removeConstraint(
      T eventStateId, Collection<E> conditionStateIds);

  /**
   * Removes a {@link ProbabilityConstraint} matching the given inputs from this {@code
   * BayesianNetwork}.
   *
   * @param eventStateIds the identifiers of the event {@link NodeState}s in the {@link
   *     ProbabilityConstraint}.
   * @param conditionStateIds the collected ids of all condition {@link NodeState}s in the {@link
   *     ProbabilityConstraint}.
   * @param <T> the class of the event {@link NodeState} identifiers.
   * @param <E> the class of the condition {@link NodeState} identifiers.
   * @return {@code true} if a matching {@link ProbabilityConstraint} existed in the network
   */
  <T extends Serializable, E extends Serializable> boolean removeConstraint(
      Collection<T> eventStateIds, Collection<E> conditionStateIds);

  /**
   * Removes all {@link ProbabilityConstraint}s from this {@code BayesianNetwork}.
   *
   * @return {@code true} if any {@link ProbabilityConstraint}s existed in the network.
   */
  boolean removeAllConstraints();

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NETWORK FUNCTIONS-------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Runs a {@link BayesSolver} instance to find the best fit for the constraints provided. Other
   * methods that involve sampling, observing, and printing from the network implicitly run this
   * method. If analysis of the solver convergence is required, consider creating a new {@link
   * BayesSolver} instance using {@code BayesSolver.create(network)}.
   *
   * @return this instance for method chaining.
   */
  BayesianNetwork solveNetwork();

  /**
   * Checks if this {@code BayesianNetwork} is in a solved configuration.
   *
   * @return {@code true} if this {@code BayesianNetwork} has been solved.
   */
  boolean isSolved();

  /**
   * Prints the CPTs of this {@code BayesianNetwork}, either to a {@code .txt} file or to the
   * console. Parameters for the printer can be defined within {@code app.properties} under the
   * {@code app.printer} section.
   *
   * @return this instance for method chaining.
   */
  BayesianNetwork printNetwork();

  /**
   * Builds the underlying data object containing all network information. Only runs if the network
   * is not solved.
   *
   * @return this instance for method chaining.
   */
  BayesianNetwork buildNetworkData();

  /**
   * Retrieves the data object containing all network information pertinent to this {@code
   * BayesianNetwork}. It is advisable to not modify this data in any way.
   *
   * @return the data object for this {@code BayesianNetwork}.
   */
  BayesianNetworkData getNetworkData();

  /**
   * Retrieves the Conditional Probability Table (CPT) for a given node.
   *
   * @param nodeID the identifier of the node.
   * @param <T> class of the Node ID
   * @return the probability table for the specified node.
   */
  <T extends Serializable> NetworkTable getNetworkTable(T nodeID);

  /**
   * Builds a new {@link InferenceEngine} from the current {@code BayesianNetwork}. An {@link
   * InferenceEngine} provides utilities for running direct inference on a network, including
   * setting observations, querying prior and posterior probabilities, and printing posterior
   * probability tables. This method will construct the default variant set in {@code
   * app.properties}, see the {@link InferenceType} documentation for further information.
   *
   * @return a new {@link InferenceEngine} referencing this {@code BayesianNetwork}.
   */
  InferenceEngine buildInferenceEngine();

  /**
   * Creates a new Monte Carlo {@link Sampler} from this {@code BayesianNetwork}. A {@link Sampler}
   * provides utilities for running indirect inference on a {@code BayesianNetwork}.
   *
   * @return a new {@link Sampler} referencing this {@code BayesianNetwork}.
   */
  Sampler buildSampler();

  /** Removes all data from this {@code BayesianNetwork}, excluding the network name. */
  void resetAllData();
}
