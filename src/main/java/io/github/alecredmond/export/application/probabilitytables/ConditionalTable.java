package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.probabilitytables.ConditionalTableQueryTool;

/**
 * A conditional probability table (CPT) within a {@link BayesianNetwork}. CPTs map the probability
 * over all {@link NodeState} values of a single {@link Node}, conditional on the states of its
 * parents {@code P(X|Pa(X))}.
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @see ProbabilityTable
 * @see NetworkTable
 * @author Alec Redmond
 */
public interface ConditionalTable extends NetworkTable {

  /**
   * {@inheritDoc}
   *
   * <p>Returns a specialized {@link ConditionalTableQueryTool} containing additional utility
   * methods specific to {@code ConditionalTable}.
   *
   * @return the conditional table query tool for this table
   */
  @Override
  ConditionalTableQueryTool getQueryTool();
}
