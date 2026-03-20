package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.exceptions.SampleValidationException;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.sampler.Sample;
import io.github.alecredmond.export.method.sampler.SampleCollection;
import java.util.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class SampleBuilder {
  private SampleCollection collection;

  public SampleCollection build(
      int numberOfSamples,
      Map<Sample, Integer> sampleMap,
      Map<Node, NodeState> observations,
      Node[] nodeArray,
      BayesianNetworkData data) {
    removeEmpty(sampleMap);
    setCounts(sampleMap);
    List<Sample> samples = radixSort(sampleMap, observations, nodeArray);
    collection = new SampleCollection(numberOfSamples, samples, observations, nodeArray, data);
    return validateSamples();
  }

  private void removeEmpty(Map<Sample, Integer> sampleMap) {
    sampleMap.entrySet().removeIf(entry -> entry.getValue() <= 0);
  }

  private void setCounts(Map<Sample, Integer> sampleMap) {
    sampleMap.forEach((sample, integer) -> sample.getSampleData().setCount(integer));
  }

  private List<Sample> radixSort(
      Map<Sample, Integer> sampleMap, Map<Node, NodeState> observations, Node[] nodeArray) {
    List<Sample> samples = new ArrayList<>(sampleMap.keySet());
    for (int r = nodeArray.length - 1; r >= 0; r--) {
      samples = bucketSortByIndex(samples, observations, nodeArray[r], r);
    }
    return samples;
  }

  public SampleCollection validateSamples() {
    try {
      sampleCountCorrect();
      return collection;
    } catch (SampleValidationException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  private List<Sample> bucketSortByIndex(
      List<Sample> samples, Map<Node, NodeState> observations, Node node, int stateIndex) {
    if (observations.containsKey(node)) return samples;
    Map<NodeState, List<Sample>> buckets = new HashMap<>();
    samples.forEach(
        sample -> {
          NodeState state = sample.getRawArray()[stateIndex];
          buckets.putIfAbsent(state, new ArrayList<>());
          buckets.get(state).add(sample);
        });
    return node.getNodeStates().stream()
        .filter(buckets::containsKey)
        .flatMap(state -> buckets.get(state).stream())
        .toList();
  }

  private void sampleCountCorrect() {
    int totalSamples = collection.size();
    List<Sample> samples = collection.getSamples();
    int sampleMapCount = samples.isEmpty() ? 0 : samples.stream().mapToInt(Sample::size).sum();
    if (totalSamples == sampleMapCount) {
      return;
    }
    throw new SampleValidationException(
        "Mismatch between expected total samples %d and counted samples %d"
            .formatted(totalSamples, sampleMapCount));
  }
}
