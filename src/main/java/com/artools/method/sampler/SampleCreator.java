package com.artools.method.sampler;

import com.artools.application.network.BayesNetData;
import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.ProbabilityTable;
import com.artools.method.probabilitytables.TableUtils;
import com.artools.method.utils.WeightedRandom;
import java.util.*;
import java.util.stream.Collectors;

public class SampleCreator {

  private final BayesNetData data;

  public SampleCreator(BayesNetData data) {
    this.data = data;
  }

  public List<List<Object>> generateSamples(
      Map<Node, NodeState> observations, Set<Node> exclusions, int numberOfSamples) {
    List<List<Object>> samples = new ArrayList<>();
    List<Node> orderedNodes = orderNodes(data.getNodes());
    buildProbabilityMaps(orderedNodes);
    for (int i = 0; i < numberOfSamples; i++) {
      samples.add(newSample(observations, exclusions, orderedNodes));
    }
    return samples;
  }

  private void buildProbabilityMaps(List<Node> orderedNodes) {
    orderedNodes.forEach(
        node -> {
          ProbabilityTable table = data.getNetworkTablesMap().get(node);
          TableUtils.buildProbabilityMap(table);
        });
  }

  private List<Node> orderNodes(List<Node> nodes) {
    Map<Node, Integer> layerMap = new HashMap<>();
    nodes.forEach(node -> calculateNodeLayer(node, layerMap));
    return layerMap.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .toList();
  }

  private int calculateNodeLayer(Node node, Map<Node, Integer> layerMap) {
    if (layerMap.containsKey(node)) return layerMap.get(node);

    int layer =
        node.getParents().stream()
            .mapToInt(parent -> calculateNodeLayer(parent, layerMap) + 1)
            .max()
            .orElse(0);

    layerMap.put(node, layer);
    return layer;
  }

  private List<Object> newSample(
      Map<Node, NodeState> observations, Set<Node> exclusions, List<Node> orderedNodes) {
    Set<NodeState> sample = new HashSet<>();
    orderedNodes.forEach(node -> sample.add(getWeightedRandomSample(observations, node, sample)));

    return sample.stream()
        .filter(state -> !exclusions.contains(state.getParentNode()))
        .map(NodeState::getStateID)
        .toList();
  }

  private Set<NodeState> getConditionStates(Node node, Set<NodeState> sample) {
    return node.getParents().stream()
        .flatMap(n -> n.getParents().stream())
        .flatMap(p -> p.getStates().stream())
        .filter(sample::contains)
        .collect(Collectors.toSet());
  }

  private NodeState getWeightedRandomSample(
      Map<Node, NodeState> observations, Node node, Set<NodeState> sample) {
    if (observations.containsKey(node)) return observations.get(node);
    Set<NodeState> conditionStates = getConditionStates(node, sample);
    Map<Set<NodeState>, Double> validEntries = getValidEntries(node, conditionStates);
    return Objects.requireNonNull(WeightedRandom.nextRandom(validEntries)).stream()
        .filter(state -> state.getParentNode().equals(node))
        .findAny()
        .orElseThrow();
  }

  private Map<Set<NodeState>, Double> getValidEntries(Node node, Set<NodeState> conditionStates) {
    return data.getNetworkTablesMap().get(node).getProbabilityMap().entrySet().stream()
        .filter(entry -> entry.getKey().containsAll(conditionStates))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
