package io.github.alecredmond.method.inference.junctiontree.handlers;

import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import java.util.*;

import io.github.alecredmond.application.probabilitytables.probabilityvector.VectorCombinationKey;
import lombok.Getter;

@Getter
public class JTATableHandler {
  protected final JunctionTreeTable table;
  protected final Map<NodeState, List<Integer>> stateIndexes;

  public JTATableHandler(JunctionTreeTable table) {
    this.table = table;
  }

  public void setObserved(Set<NodeState> evidenceInTable, boolean isObserved) {
    table.setObserved(isObserved);
    table.getObservedStates().clear();
    table.getObservedStates().addAll(evidenceInTable);

    if (!isObserved) {
      return;
    }

    double[] probabilities = table.getUnobservedVector().getProbabilities();
    double[] observed = table.getObservedVector().getProbabilities();

    Arrays.fill(observed, 0.0);
    table
        .getUtils()
        .collectIndexesWithStates(evidenceInTable)
        .forEach(index -> observed[index] = probabilities[index]);
  }

  public void marginalize() {
    table.getUtils().marginalizeTable();
  }

  public void adjustByRatio(Set<Integer> integers, double ratio) {
    double[] probs = getProbabilities();
    integers.forEach(i -> probs[i] = probs[i] * ratio);
  }

  public double[] getProbabilities() {
    return table.isObserved() ? table.getObservedProbabilities() : table.getProbabilities();
  }

  public double sumFromTable(Set<NodeState> states) {
    return sumProbabilities(getIndexes(states));
  }

  public double sumProbabilities(VectorCombinationKey combinationKey) {
    return table.getUtils().sumProbabilities(combinationKey)
  }

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

  protected Set<Integer> getIndexes(NodeState state) {
    return new HashSet<>(stateIndexes.get(checkValid(state)));
  }

  private NodeState checkValid(NodeState state) {
    if (stateIndexes.containsKey(state)) return state;
    throw new IllegalArgumentException("Invalid state for table!");
  }

  protected static double getRatio(double targetProb, double actualProb) {
    return actualProb == 0 ? 0.0 : targetProb / actualProb;
  }
}
