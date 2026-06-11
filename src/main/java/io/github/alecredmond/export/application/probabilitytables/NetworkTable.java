package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.probabilitytables.NetworkTableHelper;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;

/**
 * A conditional probability table (CPT) for a {@link Node} within a {@link BayesianNetwork}. A CPT
 * maps the probability over all {@link NodeState} values of a single {@link Node}, conditional on
 * the states of its parents: {@code P(X|Pa(X))}. There are two variants of {@code NetworkTable}:
 *
 * <ul>
 *   <li>{@link ConditionalTable} for non-root nodes, conditional on their parents
 *   <li>{@link RootNodeTable} for the root nodes, which have no parents.
 * </ul>
 *
 * <p>Instances of this interface are not thread-safe. External synchronisation is required for
 * concurrent access.
 *
 * @author Alec Redmond
 * @see ProbabilityTable
 */
public interface NetworkTable extends ProbabilityTable {
  /**
   * Returns the {@link Node} within the {@link BayesianNetwork} that this CPT measures. This is the
   * {@code X} in {@code P(X|Pa(X))}.
   *
   * @return the child or root {@link Node} measured by this CPT.
   */
  Node getNetworkNode();

  /**
   * Returns the {@link NetworkTableHelper} for this table. {@link NetworkTableHelper} extends the
   * base {@link TableHelper} with additional methods specific to network probability tables.
   *
   * @return the {@link NetworkTableHelper} for this {@code NetworkTable}.
   */
  @SuppressWarnings("rawtypes")
  NetworkTableHelper getHelper();
}
