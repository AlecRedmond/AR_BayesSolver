package io.github.alecredmond.application.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.sampler.Clique;
import io.github.alecredmond.application.sampler.Separator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

/**
 * A potential table, used as a component of the Junction Tree Algorithm (JTA) for exact inference
 * in the Bayesian Network. <br>
 * In the Junction Tree architecture, instances of this table are associated with both the {@link
 * Clique} and the {@link Separator} classes.
 */
@Getter
public class JunctionTreeTable extends ProbabilityTable {
  /**
   * A copy of the probabilities array, used when calculating observed marginals in the Junction
   * Tree Algorithm. This array holds the current state of probabilities after observations have
   * been set (clamped).
   */
  private final double[] observedProbabilities;

  /**
   * links a JunctionTreeTable index to its equivalent indexes in the Network Probability Tables it
   * was constructed from. Used for faster read/write back to the network.
   */
  private final Map<ProbabilityTable, Integer[]> indexPointerMap;

  /** The set of states that have been set as evidence (observations) in this table. */
  private final Set<NodeState> observedStates;

  /** A flag indicating whether the Network has observations applied. */
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
   * @param indexPointerMap links a JunctionTreeTable index to its equivalent indexes in the
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
      Map<ProbabilityTable, Integer[]> indexPointerMap,
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
    this.indexPointerMap = indexPointerMap;
    observedStates = new HashSet<>();
    observed = false;
  }

  /**
   * Clears old evidence and sets new evidence in the table.<br>
   * Regardless of the evidence in this table, if the network is observed, the JTA will run using
   * the observed tables to prevent overwriting the true joint probabilities.
   *
   * @param newEvidence new observations to be clamped in the table
   * @param observed whether the network has been observed.
   */
  public void setObserved(Set<NodeState> newEvidence, boolean observed) {
    this.observed = observed;
    this.observedStates.clear();
    this.observedStates.addAll(newEvidence);
  }
}
