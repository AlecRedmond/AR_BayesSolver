package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.probabilitytables.NetworkTableHelper;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;

/**
 * A conditional probability table (CPT) within a {@link BayesianNetwork}. CPTs map the probability
 * over all {@link NodeState} values of a single {@link Node}, conditional on the states of its
 * parents {@code P(X|Pa(X))}. There are two variants of {@code NetworkTable}:
 *
 * <ul>
 *   <li>{@link ConditionalTable} for nodes conditional on their parents
 *   <li>{@link RootNodeTable} for the root nodes of the network, which have no parents.
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
   * Returns the {@link Node} within the {@link BayesianNetwork} that this CPT measures. This
   * represents 'X' in {@code P(X|Pa(X)}.
   *
   * @return the child or root {@link Node} this CPT measures.
   */
  Node getNetworkNode();

  /**
   * Returns the {@link NetworkTableHelper} for this {@code NetworkTable}. {@link TableHelper}
   * classes provide additional utility methods for the table, such as querying the probability of
   * {@link NodeState} combinations and creating table copies.
   *
   * <p>This returns a {@link NetworkTableHelper}, which includes all the base methods from {@link
   * TableHelper}, along with additional methods relevant to a {@code NetworkTable}.
   *
   * @return the {@link NetworkTableHelper} for this instance.
   */
  @SuppressWarnings("rawtypes")
  NetworkTableHelper getHelper();
}
