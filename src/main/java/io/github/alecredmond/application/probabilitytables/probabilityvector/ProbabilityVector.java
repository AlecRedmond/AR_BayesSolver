package io.github.alecredmond.application.probabilitytables.probabilityvector;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;

import java.util.Map;
import lombok.Data;

@Data
public class ProbabilityVector {
  private final Node[] nodeArray;
  private final int[] numberOfStates;
  private final int[] stepMultiplier;
  private final double[] probabilities;
  private final Map<Node, Integer> nodeIndexMap;
  private final Map<NodeState, Integer> stateValueMap;

  public ProbabilityVector(
      Node[] nodeArray,
      int[] numberOfStates,
      int[] stepMultiplier,
      double[] probabilities,
      Map<Node, Integer> nodeIndexMap,
      Map<NodeState, Integer> stateValueMap) {
    this.nodeArray = nodeArray;
    this.numberOfStates = numberOfStates;
    this.stepMultiplier = stepMultiplier;
    this.probabilities = probabilities;
    this.nodeIndexMap = nodeIndexMap;
    this.stateValueMap = stateValueMap;
  }
}
