package io.github.alecredmond.method.inference;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.*;

abstract class Sampler<T> {
  protected static final Random RANDOM = new Random();

  public abstract List<List<T>> generateSamples(
      BayesianNetworkData data,
      Map<Node, NodeState> observations,
      Set<Node> excludedNodes,
      Set<Node> includedNodes,
      int numberOfSamples);

  protected List<T> convertToIDs(
      Set<NodeState> states, Set<Node> excludedNodes, Set<Node> includedNodes, Class<T> tClass) {
    return states.stream()
        .filter(ns -> !excludedNodes.contains(ns.getNode()))
        .filter(ns -> includedNodes.contains(ns.getNode()))
        .map(NodeState::getId)
        .map(tClass::cast)
        .toList();
  }

  protected <R, E extends Number> R nextRandom(Map<R, E> weights) {
    if (weights.isEmpty()) {
      throw new IllegalArgumentException("nextRandom received an empty weights map!");
    }
    double totalWeight = weights.values().stream().mapToDouble(Number::doubleValue).sum();
    double randomValue = RANDOM.nextDouble() * totalWeight;
    for (Map.Entry<R, E> entry : weights.entrySet()) {
      randomValue -= entry.getValue().doubleValue();
      if (randomValue <= 0.0) return entry.getKey();
    }
    return null;
  }
}
