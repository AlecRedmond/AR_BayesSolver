package io.github.alecredmond.method.sampler;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.method.probabilitytables.TableUtils;
import io.github.alecredmond.method.utils.WeightedRandom;

import java.util.*;
import java.util.stream.IntStream;

public class SampleCreator<T> {
  private final BayesianNetworkData data;
  private final Class<T> tClass;

  public SampleCreator(BayesianNetworkData data, Class<T> tClass) {
    this.data = data;
    this.tClass = tClass;
  }

  public List<List<T>> generateSamples(
      Map<Node, NodeState> observations,
      Set<Node> excludedNodes,
      Set<Node> includedNodes,
      int numberOfSamples) {
    Map<List<T>, Double> weights = new HashMap<>();
    recursiveWeightFinder(
        0, 1.0, observations, excludedNodes, includedNodes, weights, new ArrayList<>());
    WeightedRandom<List<T>> weightedRandom = new WeightedRandom<>(weights);
    return IntStream.range(0, numberOfSamples).mapToObj(i -> weightedRandom.nextRandom()).toList();
  }

  private void recursiveWeightFinder(
      int depth,
      double jointProb,
      Map<Node, NodeState> observations,
      Set<Node> excludeNodes,
      Set<Node> includeNodes,
      Map<List<T>, Double> weights,
      List<NodeState> currentStates) {

    if (depth == data.getNodes().size()) {
      List<T> ids = getIds(currentStates, excludeNodes, includeNodes);
      if (!weights.containsKey(ids)) weights.put(ids, 0.0);
      weights.put(ids, weights.get(ids) + jointProb);
      return;
    }

    Node node = data.getNodes().get(depth);
    List<NodeState> observedStates = getObservedStates(observations, node);
    ProbabilityTable networkTable = data.getNetworkTable(node.getNodeID());

    for (NodeState state : observedStates) {
      currentStates.add(state);
      Set<NodeState> statesInTable = TableUtils.removeRedundantStates(currentStates, networkTable);
      double tableProb = networkTable.getProbability(statesInTable);
      if (tableProb != 0) {
        recursiveWeightFinder(
            depth + 1,
            tableProb * jointProb,
            observations,
            excludeNodes,
            includeNodes,
            weights,
            currentStates);
      }
      currentStates.remove(state);
    }
  }

  private List<NodeState> getObservedStates(Map<Node, NodeState> observations, Node node) {
    return observations.containsKey(node) ? List.of(observations.get(node)) : node.getStates();
  }

  private List<T> getIds(
      List<NodeState> currentStates, Set<Node> excludeNodes, Set<Node> includeNodes) {
    return currentStates.stream()
        .filter(ns -> !excludeNodes.contains(ns.getParentNode()))
        .filter(ns -> includeNodes.contains(ns.getParentNode()))
        .map(NodeState::getStateID)
        .map(tClass::cast)
        .toList();
  }
}
