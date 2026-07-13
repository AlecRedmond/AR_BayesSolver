package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * A generic probability table for use within a Bayesian network, solver, or inference engine. Each
 * table holds a reference to its event and condition nodes, a {@link ProbabilityVector} mapping the
 * Cartesian product of all node states to a backing array, and a query tool.
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @see NetworkTable
 * @see ObservedTable
 * @author Alec Redmond
 */
public interface ProbabilityTable {

  /**
   * Returns an unmodifiable map linking each state identifier to its corresponding {@link
   * NodeState} instance within this table.
   *
   * @return a map from node state identifiers to state instances.
   */
  Map<Serializable, NodeState> getNodeStateIDMap();

  /**
   * Returns an unmodifiable map linking each node identifier to its corresponding {@link Node}
   * instance within this table.
   *
   * @return a map from node identifiers to node instances.
   */
  Map<Serializable, Node> getNodeIDMap();

  /**
   * Returns the {@link ProbabilityVector} which stores probability values for this instance. A
   * {@link ProbabilityVector} contains information used to map all possible {@link NodeState}
   * combinations within this table to an index in a double array containing the associated
   * probability value.
   *
   * @return the probability vector for this table.
   */
  ProbabilityVector getVector();

  /**
   * Returns the complete, unmodifiable set of all {@link Node}s used within this table, including
   * both conditions and events.
   *
   * @return a set of every node in this table.
   */
  Set<Node> getNodes();

  /**
   * Returns the unmodifiable set of event {@link Node}s used within this table. Events represent
   * {@code E} in the expression {@code P(E|C)}, or the child {@code X} in network CPT expressions
   * {@code P(X|Pa(X))}.
   *
   * @return a set of every event node in this table.
   */
  Set<Node> getEvents();

  /**
   * Returns the unmodifiable set of condition {@link Node}s used within this table. Conditions
   * represent {@code C} in the expression {@code P(E|C)}, or the parents {@code Pa(X)} in network
   * CPT expressions {@code P(X|Pa(X))}.
   *
   * @return a set of every condition node in this table.
   */
  Set<Node> getConditions();

  /**
   * Returns the structural name of this table. Names typically follow the pattern {@code
   * P(E1,...,En|C1,...,Cm)}, where {@code E} represents event node identifiers and {@code C}
   * represents condition node identifiers.
   *
   * @return the serialized name of this table.
   */
  Serializable getTableName();

  /**
   * Returns the base {@link ProbabilityTableQueryTool} for this table. The {@link ProbabilityTableQueryTool} provides
   * utility methods applicable to all {@code ProbabilityTable} types, such as querying
   * probabilities for {@link NodeState} combinations and creating table copies.
   *
   * @return the table query tool for this table.
   */
  ProbabilityTableQueryTool getQueryTool();

  /**
   * Returns the flat probability array backing this table. The array size matches the Cartesian
   * product of all {@link NodeState} combinations within this table.
   *
   * <p>For standard usage, prefer using the safer high-level interface provided by {@link
   * #getQueryTool()}.
   *
   * @return the underlying double array containing raw probabilities.
   */
  double[] getProbabilities();
}
