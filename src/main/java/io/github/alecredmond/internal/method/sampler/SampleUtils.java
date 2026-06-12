package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.sampler.Sample;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SampleUtils {
  private SampleUtils() {}

  public static void applyToSamples(
      SampleCollectionImpl collection, Consumer<Sample> sampleConsumer) {
    collection.getSamples().forEach(sampleConsumer);
  }

  public static int countSamplesIncludingStates(
      SampleCollectionImpl sampleCollection, Collection<NodeState> states) {
    return streamAllContaining(sampleCollection, states).mapToInt(Sample::count).sum();
  }

  private static Stream<Sample> streamAllContaining(
      SampleCollectionImpl sampleCollection, Collection<NodeState> states) {
    Set<NodeState> stateSet = new HashSet<>(states);
    return sampleCollection.getSamples().stream().filter(sample -> sample.containsAll(stateSet));
  }

  public static List<Sample> listSamplesIncludingStates(
      SampleCollectionImpl sampleCollection, Collection<NodeState> states) {
    return streamAllContaining(sampleCollection, states).toList();
  }

  public static <F extends E, E extends Collection<NodeState>> E stateArrayToCollection(
      NodeState[] stateArray, Supplier<F> supplier) {
    return Arrays.stream(stateArray).collect(Collectors.toCollection(supplier));
  }
}
