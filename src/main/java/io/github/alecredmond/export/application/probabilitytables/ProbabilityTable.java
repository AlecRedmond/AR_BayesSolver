package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Interface for a probability table used in a Bayesian Network, Solver, or Inference Engine.
 * Probability Tables contain information concerning the event and condition nodes active in the
 * table, a {@link ProbabilityVector} object which maps the Cartesian Product of all states to a
 * probability array, and a {@link TableHelper} which can be used for querying the table.
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
   * Returns a map of ids and their associated {@link NodeState} within the table.
   *
   * @return the {@link NodeState} ID map from this instance.
   */
  Map<Serializable, NodeState> getNodeStateIDMap();

  /**
   * Returns a map of ids and their associated {@link Node} within the table.
   *
   * @return the {@link Node} ID map from this instance.
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
   * Returns the name of the table. This is typically a string in the form {@code
   * "P({E_ids}|{C_ids})"} where E_ids are the ids of all event nodes, and C_ids are the ids of all
   * condition nodes.
   *
   * @return the table name for this {@code ProbabilityTable} instance.
   */
  Serializable getTableName();

  /**
   * Returns the {@link TableHelper} for this {@code ProbabilityTable}. {@link TableHelper} classes
   * provide additional utility methods for the table, such as querying the probability of {@link
   * NodeState} combinations and creating table copies.
   *
   * <p>This returns the base {@link TableHelper}, which includes methods applicable to all classes
   * that extend {@code ProbabilityTable}.
   *
   * @return the {@link TableHelper} for this instance.
   */
  @SuppressWarnings("rawtypes")
  TableHelper getHelper();

  /**
   * Returns the probability array for this {@code ProbabilityTable}. This contains a probability
   * entry for every {@link NodeState} combination in the Cartesian product of the table's nodes.
   * Further information about how these combinations are indexed can be found in the documentation
   * for {@link ProbabilityVector}. For standard queries, it is advisable to use this table's {@link
   * TableHelper}, which can be accessed through by calling {@link #getHelper()}.
   *
   * @return the probability array for this {@code ProbabilityTable}.
   */
  double[] getProbabilities();
}
