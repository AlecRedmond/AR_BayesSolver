package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.probabilitytables.ObservedTableQueryTool;
import io.github.alecredmond.export.method.probabilitytables.TableQueryTool;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A table measuring the prior or posterior probability distribution over a single {@link Node}
 * within an {@link InferenceEngine}. An {@code ObservedTable} contains one entry per {@link
 * NodeState} of the measured {@link Node}, each recording {@code P(X=x|Obs)}, where {@code X} is
 * the measured node, {@code x} is one of its possible states, and {@code Obs} is the set of
 * observations currently active on the {@link InferenceEngine}.
 *
 * <p>Instances of this interface are not thread-safe. External synchronisation is required for
 * concurrent access.
 *
 * @author Alec Redmond
 * @see ProbabilityTable
 * @see InferenceEngine
 */
public interface ObservedTable extends ProbabilityTable {
  /**
   * Returns the single {@link Node} whose prior or posterior probabilities are measured by this
   * {@code ObservedTable}.
   *
   * @return the {@link Node} this {@code ObservedTable} measures.
   */
  Node getMeasuredNode();

  /**
   * Returns the observations currently active on the {@link InferenceEngine}, as a map from each
   * observed {@link Node} to its observed {@link NodeState}. The posterior probabilities in this
   * table are conditioned on these observations. Returns an empty map if no observations are
   * present.
   *
   * @return an unmodifiable {@link LinkedHashMap} mapping each observed {@link Node} to its
   *     observed {@link NodeState}.
   */
  Map<Node, NodeState> getObservations();

  /**
   * Returns the {@link ObservedTableQueryTool} for this table. {@link ObservedTableQueryTool} extends the
   * base {@link TableQueryTool} with additional methods specific to observed probability tables.
   *
   * @return the {@link ObservedTableQueryTool} for this {@code ObservedTable}.
   */
  @Override
  ObservedTableQueryTool getQueryTool();
}
