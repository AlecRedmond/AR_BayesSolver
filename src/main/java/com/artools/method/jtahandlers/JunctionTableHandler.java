package com.artools.method.jtahandlers;

import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.JunctionTreeTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import java.util.*;
import java.util.stream.IntStream;
import lombok.Getter;

@Getter
public class JunctionTableHandler {
  protected final ProbabilityTable table;
  protected final Map<NodeState, List<Integer>> stateIndexes;

  public JunctionTableHandler(ProbabilityTable table) {
    this.table = table;
    this.stateIndexes = fillStateIndexes(table);
  }

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

  public void marginalize() {
    double[] probs = getProbabilities();
    double sum = Arrays.stream(probs).sum();
    if (sum == 0 || sum == 1.0) return;
    double ratio = 1 / sum;
    IntStream.range(0, probs.length).forEach(i -> probs[i] = probs[i] * ratio);
  }

  public double[] getProbabilities() {
    return table.getProbabilities();
  }

  public void adjustByRatio(Set<Integer> integers, double ratio) {
    double[] probs = getCorrectProbabilities();
    integers.forEach(i -> probs[i] = probs[i] * ratio);
  }

  protected double[] getCorrectProbabilities() {
    JunctionTreeTable t = (JunctionTreeTable) table;
    return t.isObserved() ? t.getObservedProbabilities() : t.getProbabilities();
  }

  public double sumFromTable(Set<NodeState> states) {
    return sumFromTableIndexes(getIndexes(states));
  }

  public double sumFromTableIndexes(Collection<Integer> indexes) {
    double[] probabilities = table.getProbabilities();
    double prob = 0.0;
    for (int index : indexes) {
      prob += probabilities[index];
    }
    return prob;
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
