package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.RootNodeTable;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A helper attached to a {@link RootNodeTable} which provides additional methods for the table.
 * This includes methods all methods found in {@link NetworkTableHelper}, alongside additional
 * methods to query probabilities using a single {@link NodeState}, and a method to return a map
 * linking each state to its unconditional probability.
 *
 * @see TableHelper
 * @see NetworkTableHelper
 * @see ConditionalTableHelper
 * @author Alec Redmond
 */
public interface RootNodeTableHelper extends NetworkTableHelper<RootNodeTable> {
  /**
   * Returns the probability associated with a state within {@link RootNodeTable#getNetworkNode()}
   * in the form:<br>
   * {@code P(X=x)}<br>
   * where {@code X} is the root node and {@code x} is one of its possible states.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, this will require
   * the given {@link NodeState} to be in the root node's state list, otherwise {@code null} will be
   * returned.
   *
   * @param state the state within the {@link RootNodeTable}'s network node to be queried.
   * @return the probability table entry associated with the input state, or {@code null} if safe
   *     mode validation fails.
   */
  Double getProbability(NodeState state);

  /**
   * Returns the probability associated with a state within {@link RootNodeTable#getNetworkNode()}
   * in the form:<br>
   * {@code P(X=x)}<br>
   * where {@code X} is the root node and {@code x} is one of its possible states.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, this will require
   * the given {@link NodeState} to be in the root node's state list, otherwise {@code null} will be
   * returned.
   *
   * @param stateId the id of the {@link NodeState} within the {@link RootNodeTable}'s network node
   *     to be queried.
   * @return the probability table entry associated with the input state, or {@code null} if safe
   *     mode validation fails.
   */
  Double getProbabilityById(Serializable stateId);

  /**
   * Returns a map of each {@link NodeState} within the table's root {@link Node}, paired to its
   * associated probability value {@code P(X=x)}. The map is a {@link LinkedHashMap} which maintains
   * the same order as {@link Node#getNodeStates()} for the root node.
   *
   * @return a new {@link LinkedHashMap} linking each {@link NodeState} in the root node to its
   *     probability table entry.
   */
  Map<NodeState, Double> buildProbabilityMap();
}
