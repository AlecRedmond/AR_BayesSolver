package io.github.alecredmond.method.inference.junctiontree.handlers;

import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.junctiontree.JunctionTreeTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import java.util.*;
import java.util.stream.IntStream;
import lombok.Getter;

/**
 * Handles operations related to a {@link JunctionTreeTable}, primarily for managing and accessing
 * probability data and index mapping within the context of a Junction Tree Algorithm (JTA). It
 * stores and provides rapid access to the indexes in the table's probability array that correspond
 * to a specific {@link NodeState}.
 */
@Getter
public class JTATableHandler {
  protected final JunctionTreeTable table;
  protected final Map<NodeState, List<Integer>> stateIndexes;

  /**
   * Constructs a new {@code JTATableHandler}.
   *
   * @param table The {@link JunctionTreeTable} this handler will manage.
   */
  public JTATableHandler(JunctionTreeTable table) {
    this.table = table;
    this.stateIndexes = fillStateIndexes(table);
  }

  /**
   * Populates the internal map that links each {@link NodeState} in the table's scope to a list of
   * indexes in the table's probability array where that state occurs.
   *
   * @param table The probability table to process.
   * @return A map of {@link NodeState} to a list of corresponding probability array indexes.
   */
  private Map<NodeState, List<Integer>> fillStateIndexes(ProbabilityTable table) {
    Map<NodeState, List<Integer>> stateIndexMap = new HashMap<>();
    for (Set<NodeState> key : table.getKeySet()) {
      int index = table.getIndexMap().get(key);
      for (NodeState state : key) {
        stateIndexMap.putIfAbsent(state, new ArrayList<>());
        stateIndexMap.get(state).add(index);
      }
    }
    return stateIndexMap;
  }

  /**
   * Sets the evidence (observed states) on the managed {@link JunctionTreeTable}. If the table is
   * set as observed, it marginalizes the observed probabilities.
   *
   * @param evidenceInTable The set of {@link NodeState}s that constitute the evidence.
   * @param isObserved A boolean indicating if the table should be treated as 'observed'.
   */
  public void setObserved(Set<NodeState> evidenceInTable, boolean isObserved) {
    table.setObserved(evidenceInTable, isObserved);
    if (!isObserved) return;
    double[] probabilities = table.getProbabilities();
    double[] observed = table.getObservedProbabilities();
    Arrays.fill(observed, 0.0);
    getIndexes(evidenceInTable).forEach(index -> observed[index] = probabilities[index]);
  }

  /**
   * Finds the set of probability array indexes that are common to all {@link NodeState} values in
   * the provided key set. This is used to find the location of the joint probability for a given
   * configuration of states.
   *
   * @param key A set of {@link NodeState} values.
   * @return A set of integers representing the shared indexes in the probability array.
   */
  public Set<Integer> getIndexes(Set<NodeState> key) {
    Set<Integer> common = new HashSet<>();
    boolean firstVal = true;
    for (NodeState state : key) {
      if (firstVal) {
        common.addAll(stateIndexes.get(state));
        firstVal = false;
        continue;
      }
      common.retainAll(new HashSet<>(stateIndexes.get(state)));
    }
    return common;
  }

  /**
   * Normalizes the probabilities in the table's current probability array (either the main or the
   * observed array) so that they sum to 1.0. It is a no-op if the sum is already 0 or 1.0.
   */
  public void marginalize() {
    double[] probs = getProbabilities();
    double sum = Arrays.stream(probs).sum();
    if (sum == 0 || sum == 1.0) return;
    double ratio = 1 / sum;
    IntStream.range(0, probs.length).forEach(i -> probs[i] = probs[i] * ratio);
  }

  /**
   * Returns the probability array being used by the table, which is either the observed
   * probabilities if the table is observed, or the main probabilities otherwise.
   *
   * @return The array of probabilities.
   */
  public double[] getProbabilities() {
    return table.isObserved() ? table.getObservedProbabilities() : table.getProbabilities();
  }

  /**
   * Adjusts the probabilities at the given indexes by multiplying them by a specified ratio.
   *
   * @param integers The set of indexes to adjust.
   * @param ratio The multiplication factor.
   */
  public void adjustByRatio(Set<Integer> integers, double ratio) {
    double[] probs = getProbabilities();
    integers.forEach(i -> probs[i] = probs[i] * ratio);
  }

  /**
   * Calculates the sum of probabilities for all entries in the table that contain the given set of
   * states.
   *
   * @param states The set of {@link NodeState} values to marginalize over.
   * @return The sum of the corresponding probabilities.
   */
  public double sumFromTable(Set<NodeState> states) {
    return sumFromTableIndexes(getIndexes(states));
  }

  /**
   * Calculates the sum of probabilities at the specified array indexes.
   *
   * @param indexes A collection of array indexes.
   * @return The sum of the probabilities at those indexes.
   */
  public double sumFromTableIndexes(Collection<Integer> indexes) {
    double[] probabilities = getProbabilities();
    double prob = 0.0;
    for (int index : indexes) {
      prob += probabilities[index];
    }
    return prob;
  }

  /**
   * Retrieves the set of probability array indexes associated with a single {@link NodeState}.
   *
   * @param state The {@link NodeState} of interest.
   * @return A set of indexes where this state occurs in the table.
   */
  protected Set<Integer> getIndexes(NodeState state) {
    return new HashSet<>(stateIndexes.get(checkValid(state)));
  }

  /**
   * Ensures the given {@link NodeState} is part of the table's state index map.
   *
   * @param state The {@link NodeState} to validate.
   * @return The validated {@link NodeState}.
   * @throws IllegalArgumentException if the state is not valid for this table.
   */
  private NodeState checkValid(NodeState state) {
    if (stateIndexes.containsKey(state)) return state;
    throw new IllegalArgumentException("Invalid state for table!");
  }

  /**
   * Calculates the ratio of the target probability to the actual probability, handling the case
   * where the actual probability is zero.
   *
   * @param targetProb The desired probability.
   * @param actualProb The current probability.
   * @return The ratio, or 0.0 if {@code actualProb} is 0.
   */
  protected static double getRatio(double targetProb, double actualProb) {
    return actualProb == 0 ? 0.0 : targetProb / actualProb;
  }
}
