package io.github.alecredmond.application.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public class JunctionTreeTable extends ProbabilityTable {
  private final double[] observedProbabilities;
  private final Map<ProbabilityTable, Integer[]> equivalentIndexMap;
  private final Set<NodeState> observedStates;
  private boolean observed;

  /**
   * A joint probability table used in the Junction Table Algorithm, both in Cliques and Separators.
   *
   * @param tableID identifier for the table, typically a String
   * @param indexMap a map that links every set of Node States to its associated probability on the
   *     array
   * @param probabilities a flat array of probability values
   * @param events all nodes associated with the table
   * @param observedProbabilities a copy of the probabilities array, used when calculating observed
   *     marginals in the Junction Tree Algorithm
   * @param equivalentIndexMap links a JunctionTreeTable index to its equivalent indexes in the
   *     Network Probability Tables it was constructed from. Used for faster read/write back to the
   *     network.
   * @param nodeStateIDMap a map which can obtain a NodeState from its ID
   * @param nodeIDMap a map which can obtain a Node from its ID
   */
  public <T> JunctionTreeTable(
      T tableID,
      Map<Set<NodeState>, Integer> indexMap,
      double[] probabilities,
      Set<Node> events,
      double[] observedProbabilities,
      Map<ProbabilityTable, Integer[]> equivalentIndexMap,
      Map<Object, NodeState> nodeStateIDMap,
      Map<Object, Node> nodeIDMap) {
    super(
        nodeStateIDMap,
        nodeIDMap,
        indexMap,
        probabilities,
        tableID,
        events,
        events,
        new HashSet<>());
    this.observedProbabilities = observedProbabilities;
    this.equivalentIndexMap = equivalentIndexMap;
    observedStates = new HashSet<>();
    observed = false;
  }

  public void setObserved(Set<NodeState> newEvidence, boolean observed) {
    this.observed = observed;
    this.observedStates.clear();
    this.observedStates.addAll(newEvidence);
  }

  public void setObservedProb(Set<NodeState> request, double newVal) {
    observedProbabilities[indexMap.get(request)] = newVal;
  }
}
