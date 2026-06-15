package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A helper attached to a {@link ConditionalTable} which provides additional methods for the table.
 * This includes methods all methods found in {@link NetworkTableHelper}, alongside an additional
 * method to obtain a map linking the Cartesian product of all {@link NodeState} values in the table
 * to their associated conditional probability value.
 *
 * @see TableHelper
 * @see NetworkTableHelper
 * @see RootNodeTableHelper
 * @author Alec Redmond
 */
public interface ConditionalTableHelper extends NetworkTableHelper<ConditionalTable> {
  /**
   * Returns a map of each {@link NodeState} combination within the table, paired to its associated
   * probability value. The key is a {@link LinkedHashSet}, ordered by the position of each state's
   * parent {@link Node} in {@link ConditionalTable#getNodes()}, with the event node in the final
   * position.
   *
   * <p>The map itself is a {@link LinkedHashMap} which maintains the same order as the conditional
   * table's probability array returned by {@link ConditionalTable#getProbabilities()}.
   *
   * @return a new {@link LinkedHashMap} linking each {@link NodeState} combination to its
   *     conditional probability.
   */
  Map<Set<NodeState>, Double> buildProbabilitySetMap();
}
