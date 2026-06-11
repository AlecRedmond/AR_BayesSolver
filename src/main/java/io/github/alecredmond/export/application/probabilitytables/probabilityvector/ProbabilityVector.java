package io.github.alecredmond.export.application.probabilitytables.probabilityvector;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * An object containing the probability values associated with a table. This table maps the
 * Cartesian product of one or several nodes to a 1-Dimensional double array. To find the array
 * index corresponding to a combination of states, the following may be applied:
 *
 * <p>{@code S = {N0i,N1j,...,Nnz} // the ith state of Node 0, jth state of Node 1, etc...}<br>
 * {@code P(S) = probabilities[index], where:}<br>
 * {@code index = (i * stepMultiplier[0]) + (j * stepMultiplier[1]) + ... + (z * stepMultiplier[n])}
 *
 * <p>The order of nodes is laid out in the {@link #nodeArray} field, and the index of each {@link
 * Node} carries over to every other array in {@code ProbabilityVector}.
 *
 * @see ProbabilityTable
 * @author Alec Redmond
 */
@Data
@AllArgsConstructor
public class ProbabilityVector {
  private final Node[] nodeArray;
  private final NodeState[][] stateArrays;
  private final int[] numberOfStates;
  private final int[] stepMultiplier;
  private final double[] probabilities;
  private final Map<Node, Integer> nodeIndexMap;
  private final Map<NodeState, Integer> stateValueMap;
}
