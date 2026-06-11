package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.probabilitytables.ObservedTableHelper;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An {@code ObservedTable} measures the prior or posterior probabilities over a single {@link Node}
 * within an {@link InferenceEngine}. This table will only contain as many entries as the number of
 * {@link NodeState} values within that {@link Node}, each of which record {@code P(n|Obs(X)}, where
 * {@code Obs(X)} are the observations present on the {@link InferenceEngine}.
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
  Node getNode();

  /**
   * Returns a map of each {@link Node} observed within an {@link InferenceEngine} and the {@link
   * NodeState} value of the observation. The posterior probabilities contained within this {@code
   * ObservedTable} are conditioned on the values of this map. This will be empty if there are no
   * observations within the {@link InferenceEngine}.
   *
   * @return an unmodifiable {@link LinkedHashMap} of the observations on this table.
   */
  Map<Node, NodeState> getObservations();

  /**
   * Returns the {@link ObservedTableHelper} for this {@code ProbabilityTable}. {@link TableHelper}
   * classes provide additional utility methods for the table, such as querying the probability of
   * {@link NodeState} combinations and creating table copies.
   *
   * <p>This returns an {@link ObservedTableHelper}, which includes all the base methods from {@link
   * TableHelper}, along with additional methods relevant to an {@link ObservedTable}.
   *
   * @return the {@link ObservedTableHelper} for this instance.
   */
  @Override
  ObservedTableHelper getHelper();
}
