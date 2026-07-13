package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.inference.InferenceEngine;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A table measuring the prior or posterior probability distribution over a single {@link Node}
 * within an {@link InferenceEngine}.
 *
 * <p>An {@code ObservedTable} contains one entry per {@link NodeState} of the measured node, each
 * recording {@code P(X=x|Obs)}, where {@code X} is the measured node, {@code x} is one of its
 * possible states, and {@code Obs} is the set of active observations on the engine.
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @author Alec Redmond
 * @see ProbabilityTable
 * @see InferenceEngine
 */
public interface ObservedTable extends ProbabilityTable {

  /**
   * Returns the single {@link Node} whose prior or posterior probabilities are measured by this
   * table.
   *
   * @return the {@link Node} this table measures.
   */
  Node getMeasuredNode();

  /**
   * Returns the observations currently active on the underlying {@link InferenceEngine}. The
   * posterior probabilities in this table are explicitly conditioned on these observations.
   *
   * @return an unmodifiable {@link LinkedHashMap} mapping each observed node to its active state,
   *     or an empty map if no observations are present.
   */
  Map<Node, NodeState> getObservations();

  /**
   * {@inheritDoc}
   *
   * <p>Returns a specialized {@link ObservedTableQueryTool} containing additional utility methods
   * specific to observed probability tables.
   *
   * @return the observed table query tool for this table.
   */
  @Override
  ObservedTableQueryTool getQueryTool();
}
