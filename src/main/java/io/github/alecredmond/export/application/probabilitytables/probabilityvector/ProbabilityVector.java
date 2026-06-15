package io.github.alecredmond.export.application.probabilitytables.probabilityvector;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Stores the probability values for a {@link ProbabilityTable}, mapping the Cartesian product of
 * one or more {@link Node}s to a one-dimensional {@code double} array. The array index for a given
 * combination of states is calculated as:
 *
 * <pre>{@code
 * // S = {N0_i, N1_j, ..., Nn_z}: state i of Node 0, state j of Node 1, ..., state z of Node n
 * index = (i * strideLengths[0]) + (j * strideLengths[1]) + ... + (z * strideLengths[n])
 * P(S) = probabilities[index]
 * }</pre>
 *
 * <p>Node ordering is defined by {@link #nodeArray}. The same index position applies consistently
 * across {@link #stateArrays}, {@link #numberOfStates}, and {@link #strideLengths}.
 *
 * @see ProbabilityTable
 * @author Alec Redmond
 */
@Data
@AllArgsConstructor
public class ProbabilityVector {
  /** The ordered array of {@link Node}s in this probability vector. */
  private final Node[] nodeArray;

  /**
   * The states available for each node, parallel to {@link #nodeArray}. {@code stateArrays[i]}
   * contains all {@link NodeState} values for {@code nodeArray[i]}. <br>
   * Accessing {@code stateArrays[i][j]} is equivalent to {@code
   * nodeArray[i].getNodeStates().get(j)}.
   */
  private final NodeState[][] stateArrays;

  /**
   * The number of {@link NodeState} values for each node, parallel to {@link #nodeArray}.
   * Equivalent to {@code stateArrays[i].length} for each index {@code i}.
   */
  private final int[] numberOfStates;

  /**
   * The stride values used to compute an index into {@link #probabilities} for a given state
   * combination, parallel to {@link #nodeArray}. The multiplier decreases along the length of the
   * array such that:<br>
   * {@code strideLengths[i+1] = strideLengths[i] / numberOfStates[i]} <br>
   * The final element will always equal 1.<br>
   * See the class-level documentation for the full indexing formula.
   */
  private final int[] strideLengths;

  /**
   * The probability value for each {@link NodeState} combination in the Cartesian product. The
   * maximum length of this array, and therefore the absolute entry limit for a {@code
   * ProbabilityVector}, is 2<sup>31</sup>&minus;1.
   */
  private final double[] probabilities;

  /** Maps each {@link Node} in this vector to its index position in {@link #nodeArray}. */
  private final Map<Node, Integer> nodeIndexMap;

  /** Maps each {@link NodeState} to its index position within its node's state array. */
  private final Map<NodeState, Integer> stateValueMap;
}
