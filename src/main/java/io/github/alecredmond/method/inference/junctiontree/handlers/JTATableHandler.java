package io.github.alecredmond.method.inference.junctiontree.handlers;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.method.node.NodeUtils;
import io.github.alecredmond.method.probabilitytables.ProbabilityVectorUtils;
import java.util.*;
import java.util.stream.IntStream;
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
    ProbabilityVector correctVector = isObserved ? table.getObservedVector() : table.getVector();
    table.getUtils().setVector(correctVector);

    if (!isObserved) {
      return;
    }

    Map<Node, NodeState> requestMap = NodeUtils.generateRequest(evidenceInTable);

    ProbabilityVectorUtils utils = table.getUtils();
    double[] probabilities = table.getVector().getProbabilities();
    double[] observed = table.getObservedVector().getProbabilities();

    Arrays.fill(observed, 0.0);
    // getIndexes(evidenceInTable).forEach(index -> observed[index] = probabilities[index]);
  }

  public void marginalize() {
    double[] probs = getProbabilities();
    double sum = Arrays.stream(probs).sum();
    if (sum == 0 || sum == 1.0) return;
    double ratio = 1 / sum;
    IntStream.range(0, probs.length).forEach(i -> probs[i] = probs[i] * ratio);
  }

  public double[] getProbabilities() {
    return table.isObserved() ? table.getObservedProbabilities() : table.getProbabilities();
  }

  public void adjustByRatio(Set<Integer> integers, double ratio) {
    double[] probs = getProbabilities();
    integers.forEach(i -> probs[i] = probs[i] * ratio);
  }

  public double sumFromTable(Set<NodeState> states) {
    return sumFromTableIndexes(getIndexes(states));
  }

  public double sumFromTableIndexes(Collection<Integer> indexes) {
    double[] probabilities = getProbabilities();
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
