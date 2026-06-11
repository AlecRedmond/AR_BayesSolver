package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import java.util.Map;
import java.util.Set;

/**
 * A helper attached to a {@link ConditionalTable} which provides additional methods for the table.
 * This includes methods all methods found in both {@link NetworkTableHelper} and {@link
 * TableHelper}.
 *
 * @author Alec Redmond
 */
public interface ConditionalTableHelper extends NetworkTableHelper<ConditionalTable> {
  /**
   * Maps each combination of {@link NodeState} values within the table to its probability value.
   */
  Map<Set<NodeState>, Double> buildProbabilitySetMap();
}
