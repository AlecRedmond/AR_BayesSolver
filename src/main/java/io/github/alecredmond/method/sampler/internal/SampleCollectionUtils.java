package io.github.alecredmond.method.sampler.internal;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.method.sampler.export.Sample;
import io.github.alecredmond.method.sampler.export.SampleCollection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SampleCollectionUtils {
  private SampleCollectionUtils() {}

  public static void setExportSamples(SampleCollection collection, Collection<Node> nodes) {
    collection.getDistinctSamples().forEach(sample -> sample.setExportNodes(nodes));
  }

  public static void resetExportNodes(SampleCollection collection) {
    collection.getDistinctSamples().forEach(Sample::resetExportNodes);
  }

  public static int countSamplesIncludingStates(
      SampleCollection sampleCollection, Collection<NodeState> states) {
    return streamAllContaining(sampleCollection, states).mapToInt(Map.Entry::getValue).sum();
  }

  private static Stream<Map.Entry<Sample, Integer>> streamAllContaining(
      SampleCollection sampleCollection, Collection<NodeState> states) {
    Set<NodeState> stateSet = new HashSet<>(states);
    return sampleCollection.getSampleMap().entrySet().stream()
        .filter(entry -> sampleContainsAll(stateSet, entry.getKey()));
  }

  private static boolean sampleContainsAll(Set<NodeState> stateSet, Sample sample) {
    return SampleUtils.getStateCollection(sample.getRawArray(), HashSet::new).containsAll(stateSet);
  }

  public static Map<Sample, Integer> buildSampleMapIncludingStates(
      SampleCollection sampleCollection, Collection<NodeState> states) {
    return streamAllContaining(sampleCollection, states)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
