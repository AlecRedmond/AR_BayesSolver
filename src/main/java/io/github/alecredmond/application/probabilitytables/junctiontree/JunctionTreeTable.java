package io.github.alecredmond.application.probabilitytables.junctiontree;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public class JunctionTreeTable extends ProbabilityTable {
  private final double[] observedProbabilities;
  private final Map<ProbabilityTable, Integer[]> indexPointerMap;
  private final Set<NodeState> observedStates;
  private boolean observed;

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
        Set.of());
    this.observedProbabilities = observedProbabilities;
    this.indexPointerMap = indexPointerMap;
    observedStates = new HashSet<>();
    observed = false;
  }

  public void setObserved(Set<NodeState> newEvidence, boolean observed) {
    this.observed = observed;
    this.observedStates.clear();
    this.observedStates.addAll(newEvidence);
  }

  public double[] getCorrectProbabilities() {
    return observedStates.isEmpty() ? this.probabilities : this.observedProbabilities;
  }
}
