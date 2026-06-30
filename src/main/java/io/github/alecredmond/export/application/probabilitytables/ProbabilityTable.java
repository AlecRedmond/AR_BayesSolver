package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.probabilitytables.TableQueryTool;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A probability table for use within a Bayesian network, solver, or inference engine. Each table
 * holds a reference to its event and condition {@link Node}s, a {@link ProbabilityVector} that maps
 * the Cartesian product of all node states to a probability array, and a {@link TableQueryTool} for
 * querying the table.
 *
 * <p>Instances of this interface are not thread-safe. External synchronisation is required for
 * concurrent access.
 *
 * @see NetworkTable
 * @see ObservedTable
 * @author Alec Redmond
 */
public interface ProbabilityTable {
  /**
   * Returns a map from each {@link NodeState} identifier to its corresponding {@link NodeState}
   * instance within this table.
   *
   * @return an unmodifiable map from {@link NodeState} identifier to {@link NodeState} instances.
   */
  Map<Serializable, NodeState> getNodeStateIDMap();

  /**
   * Returns a map from each {@link Node} identifier to its corresponding {@link Node} instance
   * within this table.
   *
   * @return an unmodifiable map from {@link Node} identifier to {@link Node} instances.
   */
  Map<Serializable, Node> getNodeIDMap();

  /**
   * Returns the {@link ProbabilityVector} which stores probability values for this instance. A
   * {@link ProbabilityVector} contains information used to map all possible {@link NodeState}
   * combinations within this table to an index in a double array containing the associated
   * probability value.
   *
   * @return the {@link ProbabilityVector} for this table.
   */
  ProbabilityVector getVector();

  /**
   * Returns the complete {@link Node} set used within this table, both conditions and events.
   *
   * @return an unmodifiable {@link LinkedHashSet} of every {@link Node} in this table.
   */
  Set<Node> getNodes();

  /**
   * Returns the event {@link Node} set used within this table. Events represent E in {@code
   * P(E|C)}, or in terms of Network CPTs, the X in {@code P(X|Pa(X))}.
   *
   * @return an unmodifiable {@link LinkedHashSet} of every event {@link Node} in this table.
   */
  Set<Node> getEvents();

  /**
   * Returns the condition {@link Node} set used within this table. Conditions represent C in {@code
   * P(E|C)}, or in terms of Network CPTs, the Pa(X) in {@code P(X|Pa(X))}.
   *
   * @return an unmodifiable {@link LinkedHashSet} of every condition {@link Node} in this table.
   */
  Set<Node> getConditions();

  /**
   * Returns the name of this table, typically of the form {@code P(E1,...,En|C1,...,Cm)}, where
   * {@code E1,...,En} are the identifiers of the event nodes and {@code C1,...,Cm} are the
   * identifiers of the condition nodes.
   *
   * @return the name of this table.
   */
  Serializable getTableName();

  /**
   * Returns the base {@link TableQueryTool} for this table. The {@link TableQueryTool} provides utility
   * methods applicable to all {@code ProbabilityTable} types, such as querying probabilities for
   * {@link NodeState} combinations and creating table copies.
   *
   * <p>Subtypes of {@code ProbabilityTable} override this method to return a more specific helper;
   * see {@link NetworkTable#getQueryTool()}, {@link ObservedTable#getQueryTool()}, etc.
   *
   * @return the {@link TableQueryTool} for this table.
   */
  TableQueryTool getQueryTool();

  /**
   * Returns the raw probability array for this table. The array contains one entry for every {@link
   * NodeState} combination in the Cartesian product of the table's nodes; see {@link
   * ProbabilityVector} for details on how combinations are indexed.
   *
   * <p>For standard queries, prefer using the {@link TableQueryTool} returned by {@link
   * #getQueryTool()}, which provides a safer, higher-level interface.
   *
   * @return the probability array for this table.
   */
  double[] getProbabilities();
}
