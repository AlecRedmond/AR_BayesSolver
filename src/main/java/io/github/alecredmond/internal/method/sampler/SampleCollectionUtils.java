package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.sampler.Sample;
import io.github.alecredmond.export.method.sampler.SampleCollection;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SampleCollectionUtils {
  private SampleCollectionUtils() {}

  public static void applyToSamples(SampleCollection collection, Consumer<Sample> sampleConsumer) {
    collection.getSamples().forEach(sampleConsumer);
  }

  public static int countSamplesIncludingStates(
      SampleCollection sampleCollection, Collection<NodeState> states) {
    return streamAllContaining(sampleCollection, states).mapToInt(Sample::size).sum();
  }

  private static Stream<Sample> streamAllContaining(
      SampleCollection sampleCollection, Collection<NodeState> states) {
    Set<NodeState> stateSet = new HashSet<>(states);
    return sampleCollection.getSamples().stream()
        .filter(sample -> sampleContainsAll(stateSet, sample));
  }

  private static boolean sampleContainsAll(Set<NodeState> stateSet, Sample sample) {
    return SampleUtils.getStateCollection(sample.getRawArray(), HashSet::new).containsAll(stateSet);
  }

  public static List<Sample> listSamplesIncludingStates(
      SampleCollection sampleCollection, Collection<NodeState> states) {
    return streamAllContaining(sampleCollection, states).toList();
  }
}
