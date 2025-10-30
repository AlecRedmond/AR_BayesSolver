package io.github.alecredmond.method.network;

import io.github.alecredmond.application.inference.InferenceEngineConfigs;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.printer.PrinterConfigs;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.ConstraintBuilderException;
import io.github.alecredmond.exceptions.NetworkStructureException;
import java.util.Collection;
import java.util.List;

/**
 * An interface that provides a toolset for building, solving, and providing direct inference on a
 * Bayesian Network.
 *
 * <p>Marginal and Conditional probability values are not directly entered into the network, but as
 * <i>constraints</i> on the network. An Iterative Proportional Fitting Procedure (IPFP) is then
 * carried out to find a 'best fit' for the network tables based on the given constraints. This is
 * accelerated using the Junction Tree Algorithm (JTA).
 *
 * <p>It is worth noting that for each node on the network linked by a constraint outside its scope
 * (i.e. not linked to a parent or child node), another virtual 'edge' is created on the solver's
 * JTA graph. If enough additional edges are created, the solver will not subdivide the graph into
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
  <T> BayesianNetwork addNode(T nodeID);

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
  <T, E> BayesianNetwork addNode(T nodeID, Collection<E> nodeStateIDs);

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
  <T> BayesianNetwork removeNode(T nodeID);

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
  <T> Node getNode(T nodeID);

  /**
   * Adds a collection of states to an existing node.
   *
   * @param nodeID the identifier of the node to modify.
   * @param nodeStateIDs a collection of state identifiers to add.
   * @throws BayesNetIDException if each nodeStateID is not unique
   * @return this instance for method chaining.
   */
  <T, E> BayesianNetwork addNodeStates(T nodeID, Collection<E> nodeStateIDs);

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
  <T, E> BayesianNetwork addNodeState(T nodeID, E nodeStateID);

  /**
   * Removes all states from a specified node.
   *
   * @param nodeID the identifier of the node whose states will be removed.
   * @param <T> the class of the Node ID
   * @return this instance for method chaining.
   */
  <T> BayesianNetwork removeNodeStates(T nodeID);

  /**
   * Removes a specific state from a node.
   *
   * @param nodeID the identifier of the node to modify.
   * @param nodeStateID the identifier of the state to remove.
   * @param <T> the class of the Node ID
   * @param <E> the class of the NodeState ID
   * @return this instance for method chaining.
   */
  <T, E> BayesianNetwork removeNodeState(T nodeID, E nodeStateID);

  /**
   * Returns a Node State from its input ID
   *
   * @param <E> class of the Node State ID
   * @param nodeStateID the Node State ID
   * @throws IllegalArgumentException if the Node State ID is not mapped to a Node State value
   * @return the Node State object associated with the ID
   */
  <E> NodeState getNodeState(E nodeStateID);

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
  <T, E> BayesianNetwork addParents(T childID, Collection<E> parentIDs);

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
  <T, E> BayesianNetwork addParent(T childID, E parentID);

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
  <T, E> BayesianNetwork removeParent(T childID, E parentID);

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
  <T> BayesianNetwork removeParents(T childID);

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
   * @throws ConstraintBuilderException <br>
   *     - if a state is not found within the data <br>
   *     - if attempting to make a state conditional on another state from the same node <br>
   *     - if probability p is outwith 0 <= p <= 1
   * @return this instance for method chaining.
   */
  <T, E> BayesianNetwork addConstraint(
      T eventStateID, Collection<E> conditionStateIDs, double probability);

  /**
   * Adds a marginal probability constraint on the network, P(event) = probability. This can be
   * either a prior probability on a node which has no parents, or a known marginal outcome on a
   * child node.
   *
   * @param eventStateID the state of the root node.
   * @param probability the prior probability value.
   * @param <T> the class of the event state ID
   * @throws ConstraintBuilderException <br>
   *     - if probability p is outwith 0 <= p <= 1 <br>
   *     - if the state is not found within the data.
   * @return this instance for method chaining.
   */
  <T> BayesianNetwork addConstraint(T eventStateID, double probability);

  /**
   * Sets the maximum number of cycles for the IPF solver. One cycle applies the algorithm once for
   * every constraint. Default == 1000
   *
   * @param cyclesLimit the maximum number of cycles.
   * @return this instance for method chaining.
   */
  BayesianNetwork solverCyclesLimit(int cyclesLimit);

  /**
   * Sets a time limit in seconds for the inference solver to run. Default == 60
   *
   * @param timeLimitSeconds the maximum time in seconds.
   * @return this instance for method chaining.
   */
  BayesianNetwork solverTimeLimit(int timeLimitSeconds);

  /**
   * Sets the interval in seconds for logging solver progress. Default == 1
   *
   * @param seconds the log interval in seconds.
   * @return this instance for method chaining.
   */
  BayesianNetwork logIntervalSeconds(int seconds);

  /**
   * Sets the convergence threshold for the solver. The solver stops when changes between iterations
   * are below this value. Default == 1E-9
   *
   * @param threshold the convergence threshold.
   * @return this instance for method chaining.
   */
  BayesianNetwork solverConvergeThreshold(double threshold);

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
   * Retrieves the configuration class for the Inference Engine, which includes configurations for
   * the Constraint Solver and Sample Generators
   *
   * @return the current InferenceEngineConfigs class used by the network
   */
  InferenceEngineConfigs getInferenceEngineConfigs();

  /**
   * Retrieves the configuration class for the Printer
   *
   * @return the current PrinterConfigs
   */
  PrinterConfigs getPrinterConfigs();

  /**
   * Sets evidence in the network by observing one or more node states. This fixes the state of
   * certain nodes before running inference.
   *
   * @param observedNodeStateIDs a collection of node states that are considered evidence.
   * @param <T> the class of the NodeState IDs
   * @return this instance for method chaining.
   */
  <T> BayesianNetwork observeNetwork(Collection<T> observedNodeStateIDs);

  /**
   * Runs the inference algorithm to compute the posterior probabilities (marginals) of all nodes.
   * This is the equivalent of running the observeNetwork method with an empty collection.
   *
   * @return this instance for method chaining.
   */
  BayesianNetwork observeMarginals();

  /**
   * Retrieves the underlying data object containing all network information.
   *
   * @return the raw data class associated with the Bayesian network.
   */
  BayesianNetworkData getNetworkData();

  /**
   * Generates random samples from the joint probability distribution defined by the network. Note
   * that this method utilizes the most recent result observations on the network, or the true
   * marginals if no observation has been made.
   *
   * @param numberOfSamples the total number of samples to generate.
   * @param excludeNodeIDs a collection of node IDs to exclude from the samples.
   * @param includeNodeIDs a collection of node IDs to include in the samples.
   * @param <T> class of NodeState IDs
   * @param <E> class of the Node IDs
   * @param tClass the class of the type T, used for type casting.
   * @return a list of samples, where each sample is a list of node states.
   */
  <T, E> List<List<T>> generateSamples(
      Collection<E> excludeNodeIDs,
      Collection<E> includeNodeIDs,
      int numberOfSamples,
      Class<T> tClass);

  /**
   * Calculates the joint probability of a set of events occurring, conditional on the current
   * observed evidence.
   *
   * @param eventStateIDs a collection of node states representing joint events within the current
   *     conditions.
   * @param <T> Class of the event state IDs
   * @return the calculated joint probability.
   */
  <T> double getProbabilityFromCurrentObservations(Collection<T> eventStateIDs);

  /**
   * Retrieves the Conditional Probability Table (CPT) for a given node.
   *
   * @param nodeID the identifier of the node.
   * @param <T> class of the Node ID
   * @return the probability table for the specified node.
   */
  <T> ProbabilityTable getNetworkTable(T nodeID);

  /**
   * Retrieves the marginal probability table for a node after an observation. This table contains
   * the posterior probabilities of the node's states.
   *
   * @param nodeID the identifier of the node.
   * @param <T> class of the Node ID
   * @return the observed marginal table for the specified node.
   */
  <T> MarginalTable getObservedTable(T nodeID);
}
