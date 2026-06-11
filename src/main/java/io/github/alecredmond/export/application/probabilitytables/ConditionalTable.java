package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.probabilitytables.ConditionalTableHelper;
import io.github.alecredmond.export.method.probabilitytables.NetworkTableHelper;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;

/**
 * A conditional probability table (CPT) within a {@link BayesianNetwork}. CPTs map the probability
 * over all {@link NodeState} values of a single {@link Node}, conditional on the states of its
 * parents {@code P(X|Pa(X))}.
 *
 * <p>Instances of this interface are not thread-safe. External synchronisation is required for
 * concurrent access.
 *
 * @see ProbabilityTable
 * @see NetworkTable
 * @author Alec Redmond
 */
public interface ConditionalTable extends NetworkTable {
  /**
   * Returns the {@link ConditionalTableHelper} for this {@code ConditionalTable}. {@link
   * TableHelper} classes provide additional utility methods for the table, such as querying the
   * probability of {@link NodeState} combinations and creating table copies.
   *
   * <p>This returns a {@link ConditionalTableHelper}, which includes all the base methods from
   * {@link TableHelper} and {@link NetworkTableHelper}, along with additional methods relevant to a
   * {@code ConditionalTable}.
   *
   * @return the {@link ConditionalTableHelper} for this instance.
   */
  @Override
  ConditionalTableHelper getHelper();
}
