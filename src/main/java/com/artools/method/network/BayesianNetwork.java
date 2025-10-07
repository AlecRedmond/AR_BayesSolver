package com.artools.method.network;

import com.artools.application.network.BayesianNetworkData;
import com.artools.application.probabilitytables.MarginalTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import com.artools.exceptions.BayesNetIDException;
import java.util.Collection;
import java.util.List;

/**
 * Represents a Bayesian Network, a probabilistic graphical model that represents a set of variables
 * and their conditional dependencies via a directed acyclic graph (DAG).
 *
 * @author AR_TOOLS
 * @version 1.0
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
   * @param nodeID the unique identifier for the node.
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
   * @throws BayesNetIDException if the nodeID or each nodeStateID is not unique
   * @return this instance for method chaining.
   */
  <T, E> BayesianNetwork addNode(T nodeID, Collection<E> nodeStateIDs);

  /**
   * Removes a node and all associated edges from the network.
   *
   * @param nodeID the identifier of the node to remove.
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
   * @throws BayesNetIDException if the nodeStateID is not unique
   * @return this instance for method chaining.
   */
  <T, E> BayesianNetwork addNodeState(T nodeID, E nodeStateID);

  /**
   * Removes all states from a specified node.
   *
   * @param nodeID the identifier of the node whose states will be removed.
   * @return this instance for method chaining.
   */
  <T> BayesianNetwork removeNodeStates(T nodeID);

  /**
   * Removes a specific state from a node.
   *
   * @param nodeID the identifier of the node to modify.
   * @param nodeStateID the identifier of the state to remove.
   * @return this instance for method chaining.
   */
  <T, E> BayesianNetwork removeNodeState(T nodeID, E nodeStateID);

  /**
   * Defines parent-child relationships by adding directed edges from parent nodes to a child node.
   *
   * @param childID the identifier of the child node.
   * @param parentIDs a collection of identifiers for the parent nodes.
   * @return this instance for method chaining.
   */
  <T, E> BayesianNetwork addParents(T childID, Collection<E> parentIDs);

  /**
   * Defines a parent-child relationship by adding a directed edge from a parent node to a child
   * node.
   *
   * @param childID the identifier of the child node.
   * @param parentID the identifier of the parent node.
   * @return this instance for method chaining.
   */
  <T, E> BayesianNetwork addParent(T childID, E parentID);

  /**
   * Removes a directed edge between a parent and a child node.
   *
   * @param childID the identifier of the child node.
   * @param parentID the identifier of the parent node to remove.
   * @return this instance for method chaining.
   */
  <T, E> BayesianNetwork removeParent(T childID, E parentID);

  /**
   * Removes all parent relationships for a given child node, removing all incoming edges from the
   * child node.
   *
   * @param childID the identifier of the child node whose parents will be removed.
   * @return this instance for method chaining.
   */
  <T> BayesianNetwork removeParents(T childID);

  /**
   * Adds a conditional probability constraint to a node's CPT. This defines P(event | conditions) =
   * probability.
   *
   * @param eventStateID the state of the child node.
   * @param conditionStateIDs the combination of parent states.
   * @param probability the conditional probability value.
   * @return this instance for method chaining.
   */
  <T, E> BayesianNetwork addConstraint(
      T eventStateID, Collection<E> conditionStateIDs, double probability);

  /**
   * Adds a prior probability constraint for a root node (a node with no parents). This defines
   * P(event) = probability.
   *
   * @param eventStateID the state of the root node.
   * @param probability the prior probability value.
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
   * Prints a representation of the network structure and its probability tables to a .txt file or
   * the console. Default location is C:\Users\{username}\AR_Tools\proportional_fitter\output\
   *
   * @param toFile if true, prints to a file; if false, prints to the console.
   * @return this instance for method chaining.
   */
  BayesianNetwork printObserved(boolean toFile);

  /**
   * Prints the results of the most recent observation (posterior probabilities) to a .txt file or
   * the console. Default location is C:\Users\{username}\AR_Tools\proportional_fitter\output\
   *
   * @param toFile if true, prints to a file; if false, prints to the console.
   * @return this instance for method chaining.
   */
  BayesianNetwork printNetwork(boolean toFile);

  /**
   * Prints the network structure to a .txt file in the specified directory.
   *
   * @param directory the path to the directory where the output file will be saved.
   * @return this instance for method chaining.
   */
  BayesianNetwork printNetwork(String directory);

  /**
   * Prints the observed results to a file in the specified directory.
   *
   * @param directory the path to the directory where the output file will be saved.
   * @return this instance for method chaining.
   */
  BayesianNetwork printObserved(String directory);

  /**
   * Sets evidence in the network by observing one or more node states. This fixes the state of
   * certain nodes before running inference.
   *
   * @param observedNodeStateIDs a collection of node states that are considered evidence.
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
   * @param tClass the class of the type T, used for type casting.
   * @return a list of samples, where each sample is a list of node states.
   */
  <T, E> List<List<T>> generateSamples(
      int numberOfSamples,
      Collection<E> excludeNodeIDs,
      Collection<E> includeNodeIDs,
      Class<T> tClass);

  /**
   * Calculates the joint probability of a specific set of events occurring. Note that this method
   * utilizes the most recent result observations on the network, or the true marginals if no
   * observation has been made.
   *
   * @param eventStates a collection of node states for which to calculate the joint probability.
   * @return the calculated joint probability.
   */
  <T> double observeProbability(Collection<T> eventStates);

  /**
   * Retrieves the Conditional Probability Table (CPT) for a given node.
   *
   * @param nodeID the identifier of the node.
   * @return the probability table for the specified node.
   */
  <T> ProbabilityTable getNetworkTable(T nodeID);

  /**
   * Retrieves the marginal probability table for a node after an observation. This table contains
   * the posterior probabilities of the node's states.
   *
   * @param nodeID the identifier of the node.
   * @return the observed marginal table for the specified node.
   */
  <T> MarginalTable getObservedTable(T nodeID);
}
