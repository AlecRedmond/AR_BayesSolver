package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.probabilitytables.NetworkTableQueryTool;
import io.github.alecredmond.export.method.probabilitytables.RootNodeTableQueryTool;

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
   * Returns the {@link RootNodeTableQueryTool} for this table. {@link RootNodeTableQueryTool} extends
   * {@link NetworkTableQueryTool} with additional methods specific to unconditional root node tables.
   *
   * @return the {@link RootNodeTableQueryTool} for this {@code RootNodeTable}.
   */
  @Override
  RootNodeTableQueryTool getQueryTool();
}
