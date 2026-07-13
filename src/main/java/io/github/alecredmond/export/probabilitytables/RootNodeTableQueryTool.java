package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A helper attached to a {@link RootNodeTable} which provides additional methods for the table.
 * This includes methods all methods found in {@link NetworkTableQueryTool}, alongside additional
 * methods to query probabilities using a single {@link NodeState}, and a method to return a map
 * linking each state to its unconditional probability.
 *
 * @see ProbabilityTableQueryTool
 * @see NetworkTableQueryTool
 * @see ConditionalTableQueryTool
 * @author Alec Redmond
 */
public interface RootNodeTableQueryTool extends NetworkTableQueryTool {
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
   * @param stateId the identifier of the {@link NodeState} within the {@link RootNodeTable}'s
   *     network node to be queried.
   * @return the probability table entry associated with the input state, or {@code null} if safe
   *     mode validation fails.
   */
  Double getProbabilityById(Serializable stateId);

  /**
   * Returns a map of each {@link NodeState} within the table's root {@link Node}, paired to its
   * associated probability value {@code P(X=x)}. The map is a {@link LinkedHashMap} which maintains
   * the same order as the same order as the {@link NodeState} list within the root {@link Node}.
   *
   * @return a new {@link LinkedHashMap} linking each {@link NodeState} in the root node to its
   *     probability table entry.
   */
  Map<NodeState, Double> buildProbabilityMap();

  /**
   * Builds a copy of the {@link RootNodeTable} this handler is connected to. This deep-copies
   * everything except the {@link Node} and {@link NodeState} values, which maintain the original
   * references.
   *
   * @return a copy of the current table.
   */
  RootNodeTable copyTable();
}
