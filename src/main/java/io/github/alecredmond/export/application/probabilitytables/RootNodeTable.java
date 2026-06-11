package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.probabilitytables.NetworkTableHelper;
import io.github.alecredmond.export.method.probabilitytables.RootNodeTableHelper;

/**
 * An unconditional probability table for a root {@link Node} within a {@link BayesianNetwork}. Root
 * nodes have no parents, so this table contains one entry per {@link NodeState} of the associated
 * {@link Node}, representing the prior probability of each state.
 *
 * <p>Instances of this interface are not thread-safe. External synchronisation is required for
 * concurrent access.
 *
 * @see ProbabilityTable
 * @see NetworkTable
 * @see ConditionalTable
 * @author Alec Redmond
 */
public interface RootNodeTable extends NetworkTable {
  /**
   * Returns the {@link RootNodeTableHelper} for this table. {@link RootNodeTableHelper} extends
   * {@link NetworkTableHelper} with additional methods specific to unconditional root node tables.
   *
   * @return the {@link RootNodeTableHelper} for this {@code RootNodeTable}.
   */
  @Override
  RootNodeTableHelper getHelper();
}
