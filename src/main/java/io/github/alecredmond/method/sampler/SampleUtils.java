package io.github.alecredmond.method.sampler;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.sampler.Sample;
import io.github.alecredmond.method.sampler.export.SampleCollection;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SampleUtils {
  private SampleUtils() {}

  public static void setExportSamples(SampleCollection collection, Collection<Node> nodes) {
    if (nodes.isEmpty()) {
      resetExportSamples(collection);
      return;
    }
    int[] nodeIndexes = getNodeIndexes(collection.getNodes(), new HashSet<>(nodes));
    collection
        .getDistinctSamples()
        .forEach(
            sample -> {
              NodeState[] rawSample = sample.getRawSampledStates();
              NodeState[] exportSample =
                  Arrays.stream(nodeIndexes).mapToObj(i -> rawSample[i]).toArray(NodeState[]::new);
              sample.setSampledStates(exportSample);
            });
  }

  public static void resetExportSamples(SampleCollection collection) {
    collection.getDistinctSamples().forEach(SampleUtils::resetSample);
  }

  private static int[] getNodeIndexes(Node[] nodes, Set<Node> nodeSet) {
    return IntStream.range(0, nodes.length).filter(i -> nodeSet.contains(nodes[i])).toArray();
  }

  public static void resetSample(Sample sample) {
    NodeState[] rawSampledStates = sample.getRawSampledStates();
    sample.setSampledStates(Arrays.copyOf(rawSampledStates, rawSampledStates.length));
  }

  public static Map<Sample, Integer> samplesIncludingStates(
      SampleCollection sampleCollection, Collection<NodeState> states) {
    return streamAllContaining(sampleCollection, states)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Stream<Map.Entry<Sample, Integer>> streamAllContaining(
      SampleCollection sampleCollection, Collection<NodeState> states) {
    Set<NodeState> stateSet = new HashSet<>(states);
    return sampleCollection.getSampleMap().entrySet().stream()
        .filter(entry -> sampleContainsAll(stateSet, entry.getKey()));
  }

  private static boolean sampleContainsAll(Set<NodeState> stateSet, Sample sample) {
    return getStateCollection(sample.getRawSampledStates(), HashSet::new).containsAll(stateSet);
  }

  public static <F extends E, E extends Collection<NodeState>> E getStateCollection(
      NodeState[] sampledStates, Supplier<F> sampleSupplier) {
    return getStateCollectionCommon(sampledStates, sampleSupplier, nodeState -> nodeState);
  }

  private static <F extends E, E extends Collection<R>, R> E getStateCollectionCommon(
      NodeState[] sampledStates,
      Supplier<F> sampleSupplier,
      Function<NodeState, R> nodeStateFunction) {
    return Arrays.stream(sampledStates)
        .map(nodeStateFunction)
        .collect(Collectors.toCollection(sampleSupplier));
  }

  public static int countIncludingStates(
      SampleCollection sampleCollection, Collection<NodeState> states) {
    return streamAllContaining(sampleCollection, states).mapToInt(Map.Entry::getValue).sum();
  }

  public static <F extends E, E extends Collection<R>, R> E getStateIdCollection(
      NodeState[] sampledStates, Supplier<F> sampleSupplier, Class<R> idClass) {
    return getStateCollectionCommon(
        sampledStates, sampleSupplier, (nodeState -> idClass.cast(nodeState.getId())));
  }

  public static <T extends Collection<E>, E extends Collection<R>, U extends T, F extends E, R>
      T getNestedSampleIds(
          SampleCollection collection,
          Supplier<U> collectionSupplier,
          Supplier<F> sampleSupplier,
          Class<R> stateIdClass) {
    return getNestedSamplesCommon(
        collection,
        collectionSupplier,
        sampleSupplier,
        nodeState -> stateIdClass.cast(nodeState.getId()));
  }

  private static <T extends Collection<E>, E extends Collection<R>, U extends T, F extends E, R>
      T getNestedSamplesCommon(
          SampleCollection collection,
          Supplier<U> collectionSupplier,
          Supplier<F> sampleSupplier,
          Function<NodeState, R> nodeStateFunction) {
    return collection.getSampleMap().entrySet().stream()
        .flatMap(entry -> repeatAddSample(entry, sampleSupplier, nodeStateFunction))
        .collect(Collectors.toCollection(collectionSupplier));
  }

  private static <F extends E, E extends Collection<R>, R> Stream<E> repeatAddSample(
      Map.Entry<Sample, Integer> entry,
      Supplier<F> sampleSupplier,
      Function<NodeState, R> nodeStateFunction) {
    E sample =
        getStateCollectionCommon(
            entry.getKey().getSampledStates(), sampleSupplier, nodeStateFunction);
    return IntStream.range(0, entry.getValue()).mapToObj(n -> sample);
  }

  public static <T extends Collection<E>, E extends Collection<NodeState>, U extends T, F extends E>
      T getNestedSamples(
          SampleCollection collection, Supplier<U> collectionSupplier, Supplier<F> sampleSupplier) {
    return getNestedSamplesCommon(collection, collectionSupplier, sampleSupplier, n -> n);
  }
}
