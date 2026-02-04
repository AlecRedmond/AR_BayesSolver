package io.github.alecredmond.method.inference;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.method.utils.WeightedRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class LikelihoodWeightingSampler<T> extends Sampler<T> {
  private final Class<T> tClass;

  LikelihoodWeightingSampler(Class<T> tClass) {
    this.tClass = tClass;
  }

  public List<List<T>> generateSamples(
      BayesianNetworkData data,
      Map<Node, NodeState> observations,
      Set<Node> excludedNodes,
      Set<Node> includedNodes,
      int numberOfSamples) {
    Map<Set<NodeState>, Double> totalSampleWeights = new HashMap<>();
    List<Node> nodes = data.getNodes();
    Map<Node, Map<Set<NodeState>, Double>> probabilityMaps =
        createProbabilityMaps(data.getNetworkTablesMap());
    Map<Node, Set<Node>> tableNodes = createTableNodes(data.getNetworkTablesMap());

    for (int i = 0; i < numberOfSamples; i++) {
      Map.Entry<Set<NodeState>, Double> weightedSample =
          generateWeightedSample(probabilityMaps, observations, nodes, tableNodes);
      Set<NodeState> sample = weightedSample.getKey();
      double weight = weightedSample.getValue();
      totalSampleWeights.putIfAbsent(sample, 0.0);
      totalSampleWeights.put(sample, totalSampleWeights.get(sample) + weight);
    }

    return distributeSamples(totalSampleWeights, numberOfSamples, excludedNodes, includedNodes);
  }

  private Map<Node, Map<Set<NodeState>, Double>> createProbabilityMaps(
      Map<Node, ProbabilityTable> probTables) {
    Map<Node, Map<Set<NodeState>, Double>> pMap = new HashMap<>();
    for (Node node : probTables.keySet()) {
      ProbabilityTable table = probTables.get(node);
      Map<Set<NodeState>, Double> tableMap = table.getUtils().generateProbabilityMap();
      pMap.put(node, tableMap);
    }
    return pMap;
  }

  private Map<Node, Set<Node>> createTableNodes(Map<Node, ProbabilityTable> networkTablesMap) {
    Map<Node, Set<Node>> tableNodes = new HashMap<>();
    networkTablesMap.forEach((node, table) -> tableNodes.put(node, table.getNodes()));
    return tableNodes;
  }

  /**
   * Performs a random walk down the chain to obtain a new set. If the walk is constrained by being
   * forced to choose the node in the evidence, the weight of the final sample is decreased
   * proportional to the likelihood of the forced path.
   */
  private Map.Entry<Set<NodeState>, Double> generateWeightedSample(
      Map<Node, Map<Set<NodeState>, Double>> probTables,
      Map<Node, NodeState> observations,
      List<Node> nodes,
      Map<Node, Set<Node>> tableNodes) {
    double weight = 1.0;
    Map<Node, NodeState> newSample = new LinkedHashMap<>();

    for (Node node : nodes) {
      Map<NodeState, Double> probOfNexState =
          generateProbOfNextStateMap(
              node, tableNodes.get(node), probTables.get(node), observations, newSample);
      NodeState newState = WeightedRandom.nextRandom(probOfNexState);
      if (observations.containsKey(node)) weight *= probOfNexState.get(newState);
      newSample.put(node, newState);
    }

    return Map.entry(new LinkedHashSet<>(newSample.values()), weight);
  }

  private List<List<T>> distributeSamples(
      Map<Set<NodeState>, Double> totalSampleWeights,
      int numberOfSamples,
      Set<Node> excludedNodes,
      Set<Node> includedNodes) {

    Map<Set<NodeState>, Double> normalizedWeights = normalizeSampleWeights(totalSampleWeights);
    Map<Set<NodeState>, Integer> results = getResultsMap(normalizedWeights, numberOfSamples);
    List<List<T>> samples = new ArrayList<>();

    results.forEach(
        (key, samplesCount) -> {
          List<T> sample = convertToIDs(key, excludedNodes, includedNodes, tClass);
          IntStream.range(0, samplesCount).forEach(i -> samples.add(sample));
        });

    return samples;
  }

  private Map<NodeState, Double> generateProbOfNextStateMap(
      Node node,
      Set<Node> tableNodes,
      Map<Set<NodeState>, Double> probMap,
      Map<Node, NodeState> observations,
      Map<Node, NodeState> sample) {
    Map<NodeState, Double> stateMap = new HashMap<>();

    Set<NodeState> tableConditions =
        sample.entrySet().stream()
            .filter(e -> tableNodes.contains(e.getKey()))
            .map(Map.Entry::getValue)
            .collect(Collectors.toCollection(HashSet::new));

    List<NodeState> validStates =
        observations.containsKey(node) ? List.of(observations.get(node)) : node.getNodeStates();

    for (NodeState state : validStates) {
      tableConditions.add(state);
      double prob = probMap.get(tableConditions);
      stateMap.put(state, prob);
      tableConditions.remove(state);
    }
    return stateMap;
  }

  private Map<Set<NodeState>, Double> normalizeSampleWeights(
      Map<Set<NodeState>, Double> sampleWeights) {
    double sum = sampleWeights.values().stream().mapToDouble(Double::doubleValue).sum();
    Map<Set<NodeState>, Double> normalized = new HashMap<>();
    sampleWeights.forEach((key, weight) -> normalized.put(key, weight / sum));
    return normalized;
  }

  private Map<Set<NodeState>, Integer> getResultsMap(
      Map<Set<NodeState>, Double> sampleWeights, int numberOfSamples) {
    Map<Set<NodeState>, Integer> results = new HashMap<>();
    Map<Set<NodeState>, Double> fractionals = new HashMap<>();

    for (Map.Entry<Set<NodeState>, Double> weightEntry : sampleWeights.entrySet()) {
      Set<NodeState> key = weightEntry.getKey();
      double weight = weightEntry.getValue();

      double idealSamples = numberOfSamples * weight;
      int baseSamples = (int) idealSamples;

      results.put(key, baseSamples);

      double fractional = idealSamples - baseSamples;
      fractionals.put(key, fractional);
    }

    int assignedSamples = results.values().stream().mapToInt(Integer::intValue).sum();
    int remainder = numberOfSamples - assignedSamples;

    List<Set<NodeState>> orderedRemainders =
        fractionals.entrySet().stream()
            .sorted(Map.Entry.<Set<NodeState>, Double>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .toList();

    for (int i = 0; i < remainder; i++) {
      int index = i % orderedRemainders.size();
      Set<NodeState> assign = orderedRemainders.get(index);
      results.put(assign, results.get(assign) + 1);
    }
    return results;
  }
}
