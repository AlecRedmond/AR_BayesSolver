package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.probabilitytables.NetworkTableHelper;
import io.github.alecredmond.export.method.probabilitytables.RootNodeTableHelper;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;

/**
 * An unconditional probability table representing a root {@link Node} within a {@link BayesianNetwork}.
 * This table contains only as many entries as the number of {@link NodeState} values in the
 * associated root {@link Node}.
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
   * Returns the {@link RootNodeTableHelper} for this {@code RootNodeTable}. {@link TableHelper}
   * classes provide additional utility methods for the table, such as querying the probability of
   * {@link NodeState} combinations and creating table copies.
   *
   * <p>This returns a {@link RootNodeTableHelper}, which includes all the base methods from {@link
   * TableHelper} and {@link NetworkTableHelper}, along with additional methods relevant to a {@code
   * RootNodeTable}.
   *
   * @return the {@link RootNodeTableHelper} for this instance.
   */
  @Override
  RootNodeTableHelper getHelper();
}
