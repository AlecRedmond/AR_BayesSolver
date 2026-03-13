package io.github.alecredmond.internal.method.probabilitytables.probabilityvector;

import io.github.alecredmond.exceptions.ProbabilityVectorFactoryException;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class ProbabilityVectorFactory {

  public ProbabilityVector build(List<Node> nodes) {
    Node[] nodesArray = nodes.toArray(new Node[0]);
    int[] cardinality = buildCardinalityArray(nodesArray);
    cardinalitySanityCheck(cardinality, nodesArray);
    int rank = Arrays.stream(cardinality).reduce(1, (x, y) -> x * y);
    int[] multiplier = buildMultiplierArray(cardinality, rank);
    double[] probability = new double[rank];
    Arrays.fill(probability, 1.0);
    Map<Node, Integer> nodeIndexMap = buildNodeIndexMap(nodesArray);
    Map<NodeState, Integer> stateValueMap = buildStateValueMap(nodesArray);
    return new ProbabilityVector(
        nodesArray, cardinality, multiplier, probability, nodeIndexMap, stateValueMap);
  }

  private int[] buildCardinalityArray(Node[] nodes) {
    int[] cardinality = new int[nodes.length];
    IntStream.range(0, nodes.length).forEach(i -> cardinality[i] = nodes[i].getNodeStates().size());
    return cardinality;
  }

  private void cardinalitySanityCheck(int[] cardinality, Node[] nodes) {
    List<Node> zeroCardinality =
        IntStream.range(0, cardinality.length)
            .filter(i -> cardinality[i] == 0)
            .mapToObj(i -> nodes[i])
            .toList();

    if (zeroCardinality.isEmpty()) {
      return;
    }
    throw new ProbabilityVectorFactoryException(
        "Attempted to build a ProbabilityVector with stateless nodes: %s"
            .formatted(NodeUtils.formatNodesToString(zeroCardinality)));
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
