package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper attached to a {@link NetworkTable} which provides additional methods for the table. This
 * provides all the methods contained within {@link TableHelper}, plus additional methods to query
 * the conditional probability of the CPTs within the network.
 *
 * @see ConditionalTableHelper
 * @see RootNodeTableHelper
 * @author Alec Redmond
 */
public interface NetworkTableHelper<T extends NetworkTable> extends TableHelper<T> {
  /**
   * Returns a cross-section of probabilities for every {@link NodeState} in the Network {@link
   * Node}, under the given conditions.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, the input must
   * contain a {@link NodeState} for every {@link Node} value in {@link
   * NetworkTable#getConditions()}, otherwise the method will return an empty {@link HashMap}.
   *
   * @param condition a collection of {@link NodeState} values which define a condition within the
   *     table
   * @return a new map of each Network node's {@link NodeState} and its probability under the given
   *     conditions, or an empty {@link HashMap} if Safe Mode validation failed.
   */
  Map<NodeState, Double> getConditionalProb(Collection<NodeState> condition);

  /**
   * Returns a cross-section of probabilities for every {@link NodeState} in the Network {@link
   * Node}, under the given conditions.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, the input must
   * contain a {@link NodeState} for every {@link Node} value in {@link
   * NetworkTable#getConditions()}, otherwise the method will return an empty {@link HashMap}.
   *
   * @param <S> the class of the {@link NodeState} ids.
   * @param conditionIDs a collection of ids, each linked to a {@link NodeState} value which defines
   *     a condition within the table
   * @return a new map of each Network node's {@link NodeState} and its probability under the given
   *     conditions, or an empty {@link HashMap} if Safe Mode validation failed.
   */
  <S extends Serializable> Map<NodeState, Double> getConditionalProbByIds(
      Collection<S> conditionIDs);
}
