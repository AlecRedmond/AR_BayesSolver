package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.exceptions.SampleValidationException;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.sampler.SampleCollectionData;
import io.github.alecredmond.export.method.sampler.Sample;
import io.github.alecredmond.export.method.sampler.SampleCollection;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.*;
import java.util.stream.IntStream;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class SampleBuilder {

  public SampleCollection build(
      int numberOfSamples,
      Map<Sample, Integer> sampleMap,
      Map<Node, NodeState> observations,
      Node[] nodeArray,
      BayesianNetworkData networkData) {

    removeEmpty(sampleMap);
    setCounts(sampleMap);

    SampleCollectionData collectionData =
        SampleCollectionData.builder()
            .totalSamples(numberOfSamples)
            .samples(radixSort(sampleMap.keySet(), observations, nodeArray))
            .networkObservations(observations)
            .nodes(nodeArray)
            .build();

    return new SampleCollection(collectionData, networkData);
  }

  private void removeEmpty(Map<Sample, Integer> sampleMap) {
    sampleMap.entrySet().removeIf(entry -> entry.getValue() <= 0);
  }

  private void setCounts(Map<Sample, Integer> sampleMap) {
    sampleMap.forEach((sample, integer) -> sample.getSampleData().setCount(integer));
  }

  private List<Sample> radixSort(
      Collection<Sample> samples, Map<Node, NodeState> observations, Node[] nodeArray) {
    try {
      return samples.stream().sorted(radixComparator(nodeArray, observations)).toList();
    } catch (SampleValidationException e) {
      log.warn("{}, USING RANDOM ORDER...", e.getMessage());
      return samples.stream().toList();
    }
  }

  private Comparator<Sample> radixComparator(Node[] nodeArray, Map<Node, NodeState> observations) {
    Map<NodeState, Integer> stateIndexes = NodeUtils.buildStateIndexMap(nodeArray);
    return IntStream.range(0, nodeArray.length)
        .filter(i -> !observations.containsKey(nodeArray[i]))
        .mapToObj(
            i -> Comparator.comparing((Sample s) -> stateIndexes.get(s.getRawStateArray()[i])))
        .reduce(Comparator::thenComparing)
        .orElseThrow(() -> new SampleValidationException("SAMPLES COULD NOT BE ORDERED"));
  }
}
