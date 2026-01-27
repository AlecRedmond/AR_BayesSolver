package io.github.alecredmond.application.probabilitytables.probabilityvector;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class ProbabilityVector {
  private final Node[] nodes;
  private final int[] cardinality;
  private final int[] multiplier;
  private final double[] probability;
  private final Map<Node, Integer> nodeIndexMap;
  private final Map<NodeState, Integer> stateValueMap;

  public ProbabilityVector(
      Node[] nodes,
      int[] cardinality,
      int[] multiplier,
      double[] probability,
      Map<Node, Integer> nodeIndexMap,
      Map<NodeState, Integer> stateValueMap) {
    this.nodes = nodes;
    this.cardinality = cardinality;
    this.multiplier = multiplier;
    this.probability = probability;
    this.nodeIndexMap = nodeIndexMap;
    this.stateValueMap = stateValueMap;
  }

  public double getProbabilityFromStates(Collection<NodeState> request) {
    int index = getIndexFromStates(request);
    return probability[index];
  }

  private int getIndexFromStates(Collection<NodeState> request) {
    checkRequestValidity(request);
    int[] key = convertToKey(request);
    return keyToIndex(key);
  }

  private void checkRequestValidity(Collection<NodeState> request) {
    Set<Node> requestNodes = request.stream().map(NodeState::getNode).collect(Collectors.toSet());
    boolean hasDuplicateNodes = request.size() != requestNodes.size();
    if (hasDuplicateNodes) {
      throw new IllegalArgumentException();
    }
    boolean incorrectRequestSize = request.size() != nodes.length;
    if (incorrectRequestSize) {
      throw new IllegalArgumentException();
    }
    boolean incorrectNodes = !Arrays.stream(nodes).allMatch(requestNodes::contains);
    if (incorrectNodes) {
      throw new IllegalArgumentException();
    }
  }

  private int[] convertToKey(Collection<NodeState> request) {
    int[] key = new int[nodes.length];
    request.forEach(
        state -> {
          int nodeIndex = nodeIndexMap.get(state.getNode());
          int stateValue = stateValueMap.get(state);
          key[nodeIndex] = stateValue;
        });
    return key;
  }

  private int keyToIndex(int[] key) {
    return Arrays.stream(key).map(i -> i * multiplier[i]).sum();
  }
}
