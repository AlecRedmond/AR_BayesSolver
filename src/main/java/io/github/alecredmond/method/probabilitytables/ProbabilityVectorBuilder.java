package io.github.alecredmond.method.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;

@Getter
public class ProbabilityVectorBuilder {

  public ProbabilityVectorBuilder() {}

  public ProbabilityVector build(Set<Node> nodeSet) {
    Node[] nodes = nodeSet.toArray(new Node[0]);
    int[] cardinality = buildCardinalityArray(nodes);
    int rank = Arrays.stream(cardinality).reduce(1, (x, y) -> x * y);
    int[] multiplier = buildMultiplierArray(cardinality, rank);
    double[] probability = new double[rank];
    Map<Node, Integer> nodeIndexMap = buildNodeIndexMap(nodes);
    Map<NodeState, Integer> stateValueMap = buildStateValueMap(nodes);

    return new ProbabilityVector(
        nodes, cardinality, multiplier, probability, nodeIndexMap, stateValueMap);
  }

  private int[] buildCardinalityArray(Node[] nodes) {
    int[] cardinality = new int[nodes.length];
    IntStream.range(0, nodes.length).forEach(i -> cardinality[i] = nodes[i].getNodeStates().size());
    return cardinality;
  }

  private int[] buildMultiplierArray(int[] cardinality, int rank) {
    int m = rank;
    int[] multiplier = new int[cardinality.length];
    for (int i = 0; i < cardinality.length; i++) {
      m /= cardinality[i];
      multiplier[i] = m;
    }
    return multiplier;
  }

  private Map<Node, Integer> buildNodeIndexMap(Node[] nodes) {
    return IntStream.range(0, nodes.length)
        .mapToObj(i -> Map.entry(nodes[i], i))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private Map<NodeState, Integer> buildStateValueMap(Node[] nodes) {
    Map<NodeState, Integer> statePositions = new HashMap<>();
    Arrays.stream(nodes)
        .map(Node::getNodeStates)
        .forEach(
            states ->
                IntStream.range(0, states.size())
                    .forEach(i -> statePositions.put(states.get(i), i)));
    return statePositions;
  }
}
