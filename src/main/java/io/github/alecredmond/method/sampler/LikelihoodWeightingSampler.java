package io.github.alecredmond.method.sampler;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.export.ProbabilityTable;
import io.github.alecredmond.application.sampler.Sample;
import io.github.alecredmond.method.sampler.export.SampleCollection;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LikelihoodWeightingSampler extends Sampler {

  public LikelihoodWeightingSampler(BayesianNetworkData data) {
    super(data);
  }

  @Override
  public SampleCollection generateSamples(Map<Node, NodeState> observations, int numberOfSamples) {
    Node[] nodes = data.getNodes().toArray(new Node[0]);
    return validateSamples(
        numberOfSamples,
        distributeSamples(
            generateWeightedRawSamples(nodes, observations, numberOfSamples), numberOfSamples),
        observations,
        nodes);
  }

  private SampleCollection validateSamples(
      int numberOfSamples,
      Map<Sample, Integer> sampleMap,
      Map<Node, NodeState> observations,
      Node[] nodeArray) {
    return new SampleValidator(
            new SampleCollection(numberOfSamples, sampleMap, observations, nodeArray, data))
        .validateSamples();
  }

  private Map<Sample, Integer> distributeSamples(
      Map<Set<NodeState>, Double> weightedSamples, int numberOfSamples) {
    Map<Sample, Integer> distributedSamples = new HashMap<>();
    Map<Sample, Double> remainderSamples = new HashMap<>();
    double ratio = getRatio(weightedSamples, numberOfSamples);
    AtomicInteger tally = new AtomicInteger(0);
    weightedSamples.forEach(
        (rawSample, weight) -> {
          Sample sample = new Sample(rawSample.toArray(NodeState[]::new));
          double adjusted = weight * ratio;
          int distributed = (int) adjusted;
          tally.addAndGet(distributed);
          distributedSamples.put(sample, distributed);
          remainderSamples.put(sample, adjusted - distributed);
        });
    allocateRemainders(remainderSamples, numberOfSamples - tally.get(), distributedSamples);
    return distributedSamples;
  }

  private Map<Set<NodeState>, Double> generateWeightedRawSamples(
      Node[] nodes, Map<Node, NodeState> observations, int numberOfSamples) {
    Map<Set<NodeState>, Double> weightedSamples = new HashMap<>();
    ProbabilityTable[] cptArray = createCptArray(nodes, data.getNetworkTablesMap());
    int[][] cptRequestIndexes = createCptRequestIndexes(nodes, cptArray);
    NodeState[] defaultSample = createDefaultSample(nodes, observations);
    IntStream.range(0, numberOfSamples)
        .forEach(
            i -> addNewSample(weightedSamples, nodes, cptArray, cptRequestIndexes, defaultSample));
    return weightedSamples;
  }

  private double getRatio(Map<Set<NodeState>, Double> weightedSamples, int numberOfSamples) {
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

  private ProbabilityTable[] createCptArray(
      Node[] nodes, Map<Node, ProbabilityTable> networkTablesMap) {
    return Arrays.stream(nodes).map(networkTablesMap::get).toArray(ProbabilityTable[]::new);
  }

  private int[][] createCptRequestIndexes(Node[] nodes, ProbabilityTable[] cptArray) {
    int[][] cptRequestIndexes = new int[nodes.length][];
    IntStream.range(0, nodes.length)
        .forEach(
            i -> {
              Set<Node> cptNodes = cptArray[i].getNodes();
              int[] nodeIndexes =
                  IntStream.range(0, nodes.length)
                      .filter(j -> cptNodes.contains(nodes[j]))
                      .toArray();
              cptRequestIndexes[i] = nodeIndexes;
            });
    return cptRequestIndexes;
  }

  private NodeState[] createDefaultSample(Node[] nodes, Map<Node, NodeState> observations) {
    NodeState[] defaultSample = new NodeState[nodes.length];
    IntStream.range(0, nodes.length)
        .forEach(i -> defaultSample[i] = observations.getOrDefault(nodes[i], null));
    return defaultSample;
  }

  private void addNewSample(
      Map<Set<NodeState>, Double> sampleWeights,
      Node[] nodes,
      ProbabilityTable[] cptArray,
      int[][] cptRequestIndexes,
      NodeState[] defaultSample) {
    NodeState[] sample = Arrays.copyOf(defaultSample, defaultSample.length);
    double weight = 1.0;
    for (int i = 0; i < nodes.length; i++) {
      weight *= selectNextState(sample, i, nodes[i], cptArray[i], cptRequestIndexes[i]);
    }
    Set<NodeState> sampleSet =
        Arrays.stream(sample).collect(Collectors.toCollection(LinkedHashSet::new));
    sampleWeights.putIfAbsent(sampleSet, 0.0);
    sampleWeights.put(sampleSet, sampleWeights.get(sampleSet) + weight);
  }

  private double selectNextState(
      NodeState[] states, int index, Node node, ProbabilityTable cpt, int[] requestIndexes) {
    if (states[index] != null) {
      return cpt.getProbability(createRequest(requestIndexes, states));
    }
    List<NodeState> request = createRequest(requestIndexes, states);
    Map<NodeState, Double> probabilityMap = new HashMap<>();
    for (NodeState state : node.getNodeStates()) {
      request.add(state);
      probabilityMap.put(state, cpt.getProbability(request));
      request.removeLast();
    }
    states[index] = nextRandom(probabilityMap);
    return 1.0;
  }

  private List<NodeState> createRequest(int[] requestIndexes, NodeState[] states) {
    return Arrays.stream(requestIndexes)
        .mapToObj(i -> Optional.ofNullable(states[i]))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
