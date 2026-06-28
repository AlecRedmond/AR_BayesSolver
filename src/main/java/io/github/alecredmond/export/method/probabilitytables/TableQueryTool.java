package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import java.io.Serializable;
import java.util.Collection;

/**
 * A helper attached to a {@link ProbabilityTable} which provides additional methods for the table.
 * This is the base table helper, available for all variants of {@link ProbabilityTable}, which
 * provides methods for probability queries, table deep-copying, and toggling {@link NodeState}
 * validation (safe mode).
 *
 * @see NetworkTableQueryTool
 * @see ObservedTableQueryTool
 * @author Alec Redmond
 */
public interface TableQueryTool {

  /**
   * Returns the probability associated with all the given states in the form {@code
   * P(e1,...,en|c1,...,cm)} where {@code e1,...,en} are the event states, and {@code c1,...,cm} the
   * condition states. The input collection {@code {e1,...,en,c1,...,cn}} is not required to be
   * ordered.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, this requires one
   * {@link NodeState} for every {@link Node} within the table, otherwise {@code null} will be
   * returned.
   *
   * @param states the collection of all states to be queried.
   * @return the probability table entry associated with the input states, or {@code null} if safe
   *     mode validation fails.
   */
  Double getProbability(Collection<NodeState> states);

  /**
   * Returns the probability associated with all the given states in the form {@code
   * P(e1,...,en|c1,...,cm)} where {@code e1,...,en} are the event states, and {@code c1,...,cm} the
   * condition states. The input collection {@code {id(e1),...,id(en),id(c1),...,id(cn)}} is not
   * required to be ordered.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, this requires one
   * {@link NodeState} for every {@link Node} within the table, otherwise {@code null} will be
   * returned.
   *
   * @param <S> the class of the {@link NodeState} identifiers.
   * @param stateIds the collection of all {@link NodeState} identifiers to be queried.
   * @return the probability table entry associated with the input states, or {@code null} if safe
   *     mode validation fails.
   */
  <S extends Serializable> Double getProbabilityFromIDs(Collection<S> stateIds);

  /**
   * Builds a copy of the {@link ProbabilityTable} this handler is connected to. This deep-copies
   * everything except the {@link Node} and {@link NodeState} values, which maintain the original
   * references.
   *
   * @return a copy of the current table.
   */
  ProbabilityTable copyTable();

  /**
   * Normalizes the {@link ProbabilityTable} this handler is connected to. For non-conditional
   * tables, this will adjust the probability array so that all values sum to 1. For conditional
   * tables, the Cartesian product of the event states will sum to 1 for each combination of
   * condition states.
   */
  void normalizeTable();

  /**
   * Sets the safe mode flag for this {@code TableQueryTool} (default: {@code true}). Safe mode runs a
   * validation check on any method that processes a {@link NodeState} collection to ensure that
   * every {@link Node} relevant to the method is queried. For example, the method {@link
   * #getProbability(Collection)} requires one {@link NodeState} for every {@link Node} in {@link
   * ProbabilityTable#getNodes()}.
   *
   * <p>This validation check accounts for the majority of compute time in most of these methods,
   * and is unnecessary in situations where the presence of all relevant states can be assured.
   * Under these circumstances, safe mode may be turned off to bypass this check.
   *
   * @param safeMode {@code true} to validate {@link NodeState} inputs, otherwise {@code false}.
   */
  void setSafeMode(boolean safeMode);
}
