package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ObservedTable;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A helper attached to an {@link ObservedTable} which provides additional methods for the table.
 * This provides all the methods contained within {@link TableHelper}, alongside additional methods
 * to query probabilities using a single {@link NodeState}, and a method to return a map linking
 * each state to its current prior or posterior probability.
 *
 * @see TableHelper
 * @see ObservedTable
 * @see InferenceEngine
 * @author Alec Redmond
 */
public interface ObservedTableHelper extends TableHelper<ObservedTable> {
  /**
   * Returns the probability associated with a state within {@link ObservedTable#getMeasuredNode()}
   * in the form {@code P(X=x|Obs)}, where {@code X} is the measured node, {@code x} is one of its
   * possible states, and {@code Obs} is the set of observations currently active on the {@link
   * InferenceEngine}.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, this will require
   * the given {@link NodeState} to be in the measured node's state list, otherwise {@code null}
   * will be returned.
   *
   * @param state the state within the {@link ObservedTable}'s measured node to be queried.
   * @return the probability table entry associated with the input state, or {@code null} if safe
   *     mode validation fails.
   */
  Double getProbability(NodeState state);

  /**
   * Returns the probability associated with a state within {@link ObservedTable#getMeasuredNode()}
   * in the form {@code P(X=x|Obs)}, where {@code X} is the measured node, {@code x} is one of its
   * possible states, and {@code Obs} is the set of observations currently active on the {@link
   * InferenceEngine}.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, this will require
   * the given {@link NodeState} to be in the measured node's state list, otherwise {@code null}
   * will be returned.
   *
   * @param stateId the identifier of a state within the {@link ObservedTable}'s measured node to be
   *     queried.
   * @return the probability table entry associated with the input state, or {@code null} if safe
   *     mode validation fails.
   */
  Double getProbabilityById(Serializable stateId);

  /**
   * Returns a map of each {@link NodeState} within the table's measured {@link Node}, paired to its
   * associated probability value {@code P(X=x|Obs)}. The map is a {@link LinkedHashMap} which
   * maintains the same order as {@link Node#getNodeStates()} for the measured node.
   *
   * @return a new {@link LinkedHashMap} linking each {@link NodeState} in the measured node to its
   *     probability table entry.
   */
  Map<NodeState, Double> buildProbabilityMap();
}
