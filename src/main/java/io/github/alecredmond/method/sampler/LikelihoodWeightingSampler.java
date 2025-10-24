package io.github.alecredmond.method.sampler;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.method.probabilitytables.TableUtils;
import io.github.alecredmond.method.utils.WeightedRandom;
import java.util.*;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LikelihoodWeightingSampler<T> extends Sampler<T> {
  private final BayesianNetworkData data;
  private final Class<T> tClass;

  public LikelihoodWeightingSampler(BayesianNetworkData data, Class<T> tClass) {
    this.data = data;
    this.tClass = tClass;
  }

  public List<List<T>> generateSamples(
      Map<Node, NodeState> observations,
      Set<Node> excludedNodes,
      Set<Node> includedNodes,
      int numberOfSamples) {
    Map<Set<NodeState>, Double> sampleWeights = new HashMap<>();
    Map<Node, ProbabilityTable> probTables = data.getNetworkTablesMap();
    List<Node> nodes = data.getNodes();

    for (int i = 0; i < numberOfSamples; i++) {
      Map.Entry<Set<NodeState>, Double> entry =
          generateWeightedSample(probTables, observations, nodes);
      updateSampleWeights(entry, sampleWeights);
    }

    return distributeSamples(sampleWeights, numberOfSamples, excludedNodes, includedNodes);
  }

  /**
   * Performs a random walk down the chain to obtain a new set. If the walk is constrained by being
   * forced to choose the node in the evidence, the weight of the final sample is decreased
   * proportional to the likelihood of the forced path.
   */
  private Map.Entry<Set<NodeState>, Double> generateWeightedSample(
      Map<Node, ProbabilityTable> probTables, Map<Node, NodeState> observations, List<Node> nodes) {
    double weight = 1.0;
    Map<Node, NodeState> sample = new HashMap<>();
    for (Node node : nodes) {
      Map<NodeState, Double> probPerState =
          generateStateProbMap(node, probTables.get(node), observations, sample);
      NodeState newState = WeightedRandom.nextRandom(probPerState);
      if (observations.containsKey(node)) weight *= probPerState.get(newState);
      sample.put(node, newState);
    }
    return Map.entry(new HashSet<>(sample.values()), weight);
  }

  private void updateSampleWeights(
      Map.Entry<Set<NodeState>, Double> entry, Map<Set<NodeState>, Double> sampleWeights) {
    if (!sampleWeights.containsKey(entry.getKey())) sampleWeights.put(entry.getKey(), 0.0);
    Set<NodeState> key = entry.getKey();
    double toAdd = entry.getValue();
    sampleWeights.put(key, sampleWeights.get(key) + toAdd);
  }

  private List<List<T>> distributeSamples(
      Map<Set<NodeState>, Double> sampleWeights,
      int numberOfSamples,
      Set<Node> excludedNodes,
      Set<Node> includedNodes) {

    Map<Set<NodeState>, Double> normalizedWeights = normalizeSampleWeights(sampleWeights);
    Map<Set<NodeState>, Integer> results = getResultsMap(normalizedWeights, numberOfSamples);
    List<List<T>> samples = new ArrayList<>();

    results.forEach(
        (key, samplesCount) -> {
          List<T> sample = convertToIDs(key, excludedNodes, includedNodes, tClass);
          IntStream.range(0, samplesCount).forEach(i -> samples.add(sample));
        });

    return samples;
  }

  private Map<NodeState, Double> generateStateProbMap(
      Node node,
      ProbabilityTable table,
      Map<Node, NodeState> observations,
      Map<Node, NodeState> sample) {
    Map<NodeState, Double> stateMap = new HashMap<>();
    Set<NodeState> tableKey =
        new HashSet<>(TableUtils.removeRedundantStates(sample.values(), table));

    List<NodeState> validStates =
        observations.containsKey(node) ? List.of(observations.get(node)) : node.getNodeStates();

    for (NodeState state : validStates) {
      tableKey.add(state);
      double prob = table.getProbability(tableKey);
      stateMap.put(state, prob);
      tableKey.remove(state);
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
