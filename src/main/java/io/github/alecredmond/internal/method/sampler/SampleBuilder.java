package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.exceptions.SampleValidationException;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.sampler.Sample;
import io.github.alecredmond.internal.application.sampler.SampleCollectionData;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.*;
import java.util.stream.IntStream;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class SampleBuilder {

  public SampleCollectionImpl build(
      int numberOfSamples,
      Map<SampleImpl, Integer> sampleMap,
      Map<Node, NodeState> observations,
      Node[] nodeArray,
      BayesianNetworkData networkData) {

    if (sampleMap.isEmpty()) numberOfSamples = 0;
    else setCounts(sampleMap);

    SampleCollectionData collectionData =
        SampleCollectionData.builder()
            .totalSamples(numberOfSamples)
            .samples(radixSort(sampleMap.keySet(), observations, nodeArray))
            .networkObservations(Collections.unmodifiableMap(observations))
            .nodes(nodeArray)
            .build();

    return new SampleCollectionImpl(collectionData, networkData);
  }

  private void setCounts(Map<SampleImpl, Integer> sampleMap) {
    sampleMap.forEach((sample, integer) -> sample.getSampleData().setCount(integer));
  }

  private <T extends Sample> List<Sample> radixSort(
      Collection<T> samples, Map<Node, NodeState> observations, Node[] nodeArray) {
    try {
      return samples.stream()
          .map(Sample.class::cast)
          .sorted(radixComparator(nodeArray, observations))
          .toList();
    } catch (SampleValidationException e) {
      log.warn("{}, USING RANDOM ORDER...", e.getMessage());
      return samples.stream().map(Sample.class::cast).toList();
    }
  }

  private Comparator<Sample> radixComparator(Node[] nodeArray, Map<Node, NodeState> observations) {
    Map<NodeState, Integer> stateIndexes = NodeUtils.buildStateIndexMap(nodeArray);
    return IntStream.range(0, nodeArray.length)
        .filter(i -> !observations.containsKey(nodeArray[i]))
        .mapToObj(i -> Comparator.comparing((Sample s) -> stateIndexes.get(s.getAllStates()[i])))
        .reduce(Comparator::thenComparing)
        .orElseThrow(() -> new SampleValidationException("SAMPLES COULD NOT BE ORDERED"));
  }
}
