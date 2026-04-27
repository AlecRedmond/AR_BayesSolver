package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;
import io.github.alecredmond.export.method.sampler.Sample;
import io.github.alecredmond.export.method.sampler.SampleCollection;
import io.github.alecredmond.internal.application.sampler.LikelihoodWeightingSamplerData;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LikelihoodWeightingSampler extends SamplerImpl {
  private final LikelihoodWeightingSamplerData samplerData;

  public LikelihoodWeightingSampler(BayesianNetwork network, InferenceEngine engine) {
    super(network, engine);
    this.samplerData = buildSamplerData();
  }

  private LikelihoodWeightingSamplerData buildSamplerData() {
    Node[] nodes = networkData.getNodes().toArray(Node[]::new);
    TableHelper<?>[] helpers = new TableHelper[nodes.length];
    Map<Node, ProbabilityTable> networkTables = networkData.getNetworkTablesMap();
    IntStream.range(0, nodes.length)
        .forEach(i -> helpers[i] = networkTables.get(nodes[i]).getHelper());
    return new LikelihoodWeightingSamplerData(nodes, helpers);
  }

  @Override
  public SampleCollection generateWithEvidence(
      Map<Node, NodeState> observations, int numberOfSamples) {
    if (numberOfSamples < 0) {
      log.error("Attempted to generate less than zero samples!");
      return null;
    }
    initSamplerData(observations, numberOfSamples);
    generateWeightedStateSets();
    convertSetsToSamples();
    distributeSamples();
    return new SampleBuilder()
        .build(
            numberOfSamples,
            samplerData.getDistributedSamples(),
            observations,
            samplerData.getNodes(),
            networkData);
  }

  private void initSamplerData(Map<Node, NodeState> observations, int numberOfSamples) {
    samplerData.setObservations(observations);
    samplerData.setNumberOfSamples(numberOfSamples);
    samplerData.setDefaultSample(buildDefaultSample(observations, samplerData.getNodes()));
    samplerData.setWeightedStateSets(new HashMap<>());
    samplerData.setWeightedSamples(new HashMap<>());
    samplerData.setDistributedSamples(new HashMap<>());
  }

  private void generateWeightedStateSets() {
    NodeState[] defaultSample = samplerData.getDefaultSample();
    Node[] nodes = samplerData.getNodes();
    TableHelper<?>[] helpers = samplerData.getTableHelpers();
    Map<Set<NodeState>, Double> weightedStateSets = samplerData.getWeightedStateSets();

    for (int s = 0; s < samplerData.getNumberOfSamples(); s++) {
      Set<NodeState> newSet = new HashSet<>();
      double weight =
          IntStream.range(0, nodes.length)
              .mapToDouble(i -> selectNextState(newSet, defaultSample[i], helpers[i]))
              .reduce(1.0, (a, b) -> a * b);
      weightedStateSets.putIfAbsent(newSet, 0.0);
      weightedStateSets.put(newSet, weightedStateSets.get(newSet) + weight);
    }
  }

  private void convertSetsToSamples() {
    Map<Sample, Double> weightedSamples = samplerData.getWeightedSamples();
    Map<Set<NodeState>, Double> weightedStateSets = samplerData.getWeightedStateSets();
    weightedStateSets.forEach(
        (set, weight) -> weightedSamples.put(new Sample(set.toArray(NodeState[]::new)), weight));
  }

  private void distributeSamples() {
    Map<Sample, Double> weightedSamples = samplerData.getWeightedSamples();
    int numberOfSamples = samplerData.getNumberOfSamples();
    Map<Sample, Integer> distributedSamples = samplerData.getDistributedSamples();
    Map<Sample, Double> remainderSamples = new HashMap<>();
    double ratio = getRatio(weightedSamples, numberOfSamples);
    AtomicInteger tally = new AtomicInteger(0);

    weightedSamples.forEach(
        (sample, weight) -> {
          double adjusted = weight * ratio;
          int distributed = (int) adjusted;
          tally.addAndGet(distributed);
          distributedSamples.put(sample, distributed);
          remainderSamples.put(sample, adjusted - distributed);
        });

    allocateRemainders(remainderSamples, numberOfSamples - tally.get(), distributedSamples);
  }

  private NodeState[] buildDefaultSample(Map<Node, NodeState> observations, Node[] nodes) {
    return Arrays.stream(nodes)
        .map(node -> observations.getOrDefault(node, null))
        .toArray(NodeState[]::new);
  }

  private double selectNextState(
      Set<NodeState> newSample, NodeState observedNextState, TableHelper<?> helper) {
    if (observedNextState != null) {
      newSample.add(observedNextState);
      return helper.getProbability(newSample);
    }
    Map<NodeState, Double> probabilityMap = helper.getConditionalProb(newSample);
    newSample.add(nextRandom(probabilityMap));
    return 1.0;
  }

  private double getRatio(Map<Sample, Double> weightedSamples, int numberOfSamples) {
    double sum = weightedSamples.values().stream().mapToDouble(Double::doubleValue).sum();
    return sum != 0.0 ? numberOfSamples / sum : 0.0;
  }

  private void allocateRemainders(
      Map<Sample, Double> remainderWeights,
      int unallocated,
      Map<Sample, Integer> distributedWeights) {
    remainderWeights.entrySet().stream()
        .sorted(Comparator.comparingDouble(Map.Entry<Sample, Double>::getValue).reversed())
        .map(Map.Entry::getKey)
        .limit(unallocated)
        .forEach(key -> distributedWeights.put(key, distributedWeights.get(key) + 1));
  }
}
