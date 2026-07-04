package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.probabilitytables.RootNodeTableQueryTool;

/**
 * An unconditional probability table for a root {@link Node} within a {@link BayesianNetwork}. Root
 * nodes have no parents; therefore, this table contains exactly one entry per {@link NodeState} of
 * the associated node, defining the explicit prior probability distribution of each state.
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @see ProbabilityTable
 * @see NetworkTable
 * @see ConditionalTable
 * @author Alec Redmond
 */
public interface RootNodeTable extends NetworkTable {

  /**
   * {@inheritDoc}
   *
   * <p>Returns a specialized {@link RootNodeTableQueryTool} containing additional utility methods
   * specific to unconditional root node tables.
   *
   * @return the root node table query tool for this table.
   */
  @Override
  RootNodeTableQueryTool getQueryTool();
}
