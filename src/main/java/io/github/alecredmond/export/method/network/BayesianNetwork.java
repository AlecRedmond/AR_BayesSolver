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
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.method.sampler.SampleCollection;
import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;
import io.github.alecredmond.internal.serialization.NetworkFileIO;
import io.github.alecredmond.internal.serialization.mapper.SerializationMapper;
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
 * @author AR_TOOLS
 * @version 0.1.0-ALPHA
 */
@SuppressWarnings("unused")
public interface BayesianNetwork {

  /**
   * Creates a new, empty Bayesian Network with the default name "UNNAMED NETWORK".
   *
   * @return a new BayesianNetwork instance.
   */
  static BayesianNetwork newNetwork() {
    return new BayesianNetworkImpl();
  }

  /**
   * Creates a new, empty Bayesian Network with a specified name.
   *
   * @param networkName the name for the new network.
   * @return a new BayesianNetwork instance.
   */
  static BayesianNetwork newNetwork(String networkName) {
    return new BayesianNetworkImpl(networkName);
  }

  /**
   * Loads a saved BayesianNetwork from a JFileChooser window
   *
   * @return a loaded BayesianNetwork instance
   */
  static BayesianNetwork loadNetworkFromFile() {
    return new NetworkFileIO(new SerializationMapper()).loadNetwork();
  }

  /**
   * Loads a saved BayesianNetwork.
   *
   * @param file the selected .bayes file to load
   * @return a loaded BayesianNetwork instance
   */
  static BayesianNetwork loadNetworkFromFile(File file) {
    return new NetworkFileIO(new SerializationMapper()).loadNetwork(file);
  }

  /**
   * Loads a saved BayesianNetwork.
   *
   * @param filePath the absolute path to a .bayes file
   * @return a loaded BayesianNetwork instance
   */
  static BayesianNetwork loadNetworkFromFile(String filePath) {
    return new NetworkFileIO(new SerializationMapper()).loadNetwork(filePath);
  }

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
   * @return this instance for method chaining.
   */
  BayesianNetwork removeNode(Node node);

  /**
   * Removes a node and all associated edges from the network.
   *
   * @param nodeID the identifier of the node to remove.
   * @param <T> the class of the Node ID
   * @return this instance for method chaining.
   */
  <T extends Serializable> BayesianNetwork removeNodeByID(T nodeID);

  /**
   * Removes all nodes from the network, resetting it to an empty state.
   *
   * @return this instance for method chaining.
   */
  BayesianNetwork removeAllNodes();

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
   * Adds a collection of states to an existing node.
   *
   * @param nodeID the identifier of the node to modify.
   * @param nodeStateIDs a collection of state identifiers to add.
   * @throws BayesNetIDException if each nodeStateID is not unique
   * @return this instance for method chaining.
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork addNodeStates(
      T nodeID, Collection<E> nodeStateIDs);

  /**
   * Adds a single state to an existing node.
   *
   * @param nodeID the identifier of the node to modify.
   * @param nodeStateID the state identifier to add.
   * @param <T> the class of the Node ID
   * @param <E> the class of the NodeState ID
   * @throws BayesNetIDException if the nodeStateID is not unique
   * @return this instance for method chaining.
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork addNodeState(
      T nodeID, E nodeStateID);

  /**
   * Removes all states from a specified node.
   *
   * @param nodeID the identifier of the node whose states will be removed.
   * @param <T> the class of the Node ID
   * @return this instance for method chaining.
   */
  <T extends Serializable> BayesianNetwork removeNodeStates(T nodeID);

  /**
   * Removes a specific state from a node.
   *
   * @param nodeID the identifier of the node to modify.
   * @param nodeStateID the identifier of the state to remove.
   * @param <T> the class of the Node ID
   * @param <E> the class of the NodeState ID
   * @return this instance for method chaining.
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork removeNodeState(
      T nodeID, E nodeStateID);

  /**
   * Returns a Node State from its input ID
   *
   * @param <E> class of the Node State ID
   * @param nodeStateID the Node State ID
   * @throws IllegalArgumentException if the Node State ID is not mapped to a Node State value
   * @return the Node State object associated with the ID
   */
  <E extends Serializable> NodeState getNodeState(E nodeStateID);

  /**
   * Returns a set of NodeStates from their input IDs
   *
   * @param <E> class of the Node State IDs
   * @param nodeStateIDs the Node State IDs
   * @throws IllegalArgumentException if the Node State IDs are not mapped to a Node State value
   * @return a set of Node State objects associated with their IDs
   */
  <E extends Serializable> Set<NodeState> getNodeStates(Collection<E> nodeStateIDs);

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
  BayesianNetwork addParent(Node child, Node parent);

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
  <T extends Serializable, E extends Serializable> BayesianNetwork addParent(T childID, E parentID);

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
   * @throws IllegalArgumentException if a state is not found within the data
   * @throws ConstraintValidationException <br>
   *     - if attempting to make a state conditional on another state from the same node <br>
   *     - if probability p is outwith 0 <= p <= 1
   * @return this instance for method chaining.
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork addConstraint(
      T eventStateID, Collection<E> conditionStateIDs, double probability);

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
   * @return this instance for chaining
   */
  BayesianNetwork addConstraint(ProbabilityConstraint probabilityConstraint);

  /**
   * Adds a collection probability constraints to the network.
   *
   * @param probabilityConstraints a collection of ProbabilityConstraint objects such as {@link
   *     MarginalConstraint} or {@link ConditionalConstraint}
   * @throws ConstraintValidationException <br>
   *     - if probability p is outwith 0 <= p <= 1 <br>
   *     - if attempting to make a state conditional on another state from the same node <br>
   *     - if attempting to add a MarginalConstraint with non-empty conditions
   * @return this instance for chaining
   */
  BayesianNetwork addConstraints(Collection<ProbabilityConstraint> probabilityConstraints);

  /**
   * Searches the network for a marginal constraint with an event NodeState matching the given ID.
   *
   * @param eventStateId the id of the NodeState of a marginal constraint
   * @param <T> the class of the event state ID
   * @throws IllegalArgumentException if the state is not found within the data.
   * @return the associated MarginalConstraint, or null if it does not exist
   */
  <T extends Serializable> MarginalConstraint getConstraint(T eventStateId);

  /**
   * Searches the network for a conditional or marginal constraint with event and condition
   * NodeStates matching the given IDs.
   *
   * @param eventStateId the id of the NodeState of a marginal constraint
   * @param <T> the class of the event state ID
   * @throws IllegalArgumentException if the states are not found within the data.
   * @return the associated constraint, or null if it does not exist
   */
  <T extends Serializable, E extends Serializable> ProbabilityConstraint getConstraint(
      T eventStateId, Collection<E> conditionStateIds);

  /**
   * Removes a constraint from the network.
   *
   * @param probabilityConstraint the constraint to remove
   * @return true if the constraint existed in the network
   */
  boolean removeConstraint(ProbabilityConstraint probabilityConstraint);

  /**
   * Removes a marginal constraint from the network.
   *
   * @param eventStateId the id of the event NodeState in the constraint
   * @param <T> the class of the event NodeState's id.
   * @throws IllegalArgumentException if the state is not found within the data.
   * @return true if the constraint existed in the network
   */
  <T extends Serializable> boolean removeConstraint(T eventStateId);

  /**
   * Removes a constraint from the network.
   *
   * @param eventStateId the id of the event NodeState in the constraint
   * @param <T> the class of the event NodeState's id.
   * @param conditionStateIds the collected ids of all condition NodeStates in the constraint
   * @param <E> the class of the condition NodeState ids
   * @throws IllegalArgumentException if the states are not found within the data.
   * @return true if the constraint existed in the network
   */
  <T extends Serializable, E extends Serializable> boolean removeConstraint(
      T eventStateId, Collection<E> conditionStateIds);

  /**
   * Removes all constraints defined on the network.
   *
   * @return true if any constraints existed in the network
   */
  boolean removeAllConstraints();

  /**
   * Runs the Junction Table IPF solver to find the best fit for the constraints provided. Other
   * methods that involve sampling, observing, and printing from the network implicitly run this
   * method
   *
   * @return this instance for method chaining.
   */
  BayesianNetwork solveNetwork();

  /**
   * Prints a representation of the network structure and its probability tables according to
   * current printer configurations.
   *
   * @return this instance for method chaining.
   */
  BayesianNetwork printObserved();

  /**
   * Prints the results of the most recent observation (posterior probabilities) according to
   * current printer configurations.
   *
   * @return this instance for method chaining.
   */
  BayesianNetwork printNetwork();

  /**
   * Sets evidence in the network by observing one or more node states. This fixes the state of
   * certain nodes before running inference.
   *
   * @param observedNodeStateIDs a collection of node states that are considered evidence.
   * @param <T> the class of the NodeState IDs
   * @return this instance for method chaining.
   */
  <T extends Serializable> BayesianNetwork observeNetwork(Collection<T> observedNodeStateIDs);

  /**
   * Sets evidence in the network by observing one node state as true. This fixes the state of
   * certain nodes before running inference.
   *
   * @param observedNodeStateID the id of the state fixed as observed
   * @param <T> the class of the NodeState ID.
   * @return this instance for method chaining.
   */
  <T extends Serializable> BayesianNetwork observeNetwork(T observedNodeStateID);

  /**
   * Runs the inference algorithm to compute the posterior probabilities (marginals) of all nodes.
   * This is the equivalent of running the observeNetwork method with an empty collection.
   *
   * @return this instance for method chaining.
   */
  BayesianNetwork observeMarginals();

  /**
   * Builds the underlying data object containing all network information. Only runs if the network
   * is not solved.
   *
   * @return this instance for method chaining.
   */
  BayesianNetwork buildNetworkData();

  /**
   * Retrieves the underlying data object containing all network information.
   *
   * @return the raw data class associated with the Bayesian network.
   */
  BayesianNetworkData getNetworkData();

  /**
   * Generates random samples from the joint probability distribution defined by the network. Note
   * that this method utilizes the most recent result observations on the network, or the base
   * marginals if no observation has been made.
   *
   * @param numberOfSamples the total number of samples to generate.
   * @return a SampleCollection object providing further methods
   */
  SampleCollection generateSamples(int numberOfSamples);

  /**
   * Generates random samples from the joint probability distribution defined by the network. This
   * version of the method allows an observation to be sampled <b>without changing the observation
   * status of the network itself</b>.
   *
   * @param numberOfSamples the total number of samples to generate.
   * @param observedStateIDs ids of the NodeStates to be treated as observed
   * @param <T> class of the NodeState ids.
   * @return a SampleCollection object providing further methods
   */
  <T extends Serializable> SampleCollection generateSamples(
      int numberOfSamples, Collection<T> observedStateIDs);

  /**
   * Calculates the joint probability of a set of events occurring, conditional on the current
   * observed evidence.
   *
   * @param eventStateIDs a collection of node states representing joint events within the current
   *     conditions.
   * @param <T> Class of the event state IDs
   * @return the calculated joint probability.
   */
  <T extends Serializable> double getProbabilityFromCurrentObservations(
      Collection<T> eventStateIDs);

  /**
   * Retrieves the Conditional Probability Table (CPT) for a given node.
   *
   * @param nodeID the identifier of the node.
   * @param <T> class of the Node ID
   * @return the probability table for the specified node.
   */
  <T extends Serializable> ProbabilityTable getNetworkTable(T nodeID);

  /**
   * Retrieves the marginal probability table for a node after an observation. This table contains
   * the posterior probabilities of the node's states.
   *
   * @param nodeID the identifier of the node.
   * @param <T> class of the Node ID
   * @return the observed marginal table for the specified node.
   */
  <T extends Serializable> MarginalTable getObservedTable(T nodeID);
}
