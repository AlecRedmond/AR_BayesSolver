package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;

/**
 * A helper attached to a {@link ConditionalTable} which provides additional methods for the table.
 * This includes methods all methods found in {@link NetworkTableQueryTool}, alongside an additional
 * method to obtain a map linking the Cartesian product of all {@link NodeState} values in the table
 * to their associated conditional probability value.
 *
 * @see TableQueryTool
 * @see NetworkTableQueryTool
 * @see RootNodeTableQueryTool
 * @author Alec Redmond
 */
public interface ConditionalTableQueryTool extends NetworkTableQueryTool {
  /**
   * Builds a copy of the {@link ConditionalTable} this handler is connected to. This deep-copies
   * everything except the {@link Node} and {@link NodeState} values, which maintain the original
   * references.
   *
   * @return a copy of the current table.
   */
  ConditionalTable copyTable();
}
