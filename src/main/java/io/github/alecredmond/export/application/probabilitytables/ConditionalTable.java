package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.method.probabilitytables.ConditionalTableHelper;

/**
 * A table mapping the distribution of probabilities over all states of a {@link Node}, conditional
 * on its parent states. This is used as the CPT for non-root nodes in a Bayesian Network. The table
 * contains a helper object for querying the probability distribution.
 *
 * @see ProbabilityTable
 */
public interface ConditionalTable extends NetworkTable {
  @Override
  ConditionalTableHelper getHelper();
}
