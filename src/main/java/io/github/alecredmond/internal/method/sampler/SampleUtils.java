package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.sampler.SampleData;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SampleUtils {
  private SampleUtils() {}

  public static <R extends Collection<NodeState>> void setSampleSupplier(
      SampleData sampleData, Supplier<R> supplier) {
    if (supplier.equals(sampleData.getSupplier())) {
      return;
    }
    sampleData.setSupplier(supplier);
    rebuildStateCollection(sampleData, supplier);
  }

  public static <T extends Collection<NodeState>, R extends T> T rebuildStateCollection(
      SampleData sampleData, Supplier<R> supplier) {
    T newCollection = getStateCollectionCommon(sampleData.getExportArray(), supplier, s -> s);
    sampleData.setStateCollection(newCollection);
    return newCollection;
  }

  private static <F extends E, E extends Collection<R>, R> E getStateCollectionCommon(
      NodeState[] sampledStates,
      Supplier<F> sampleSupplier,
      Function<NodeState, R> nodeStateFunction) {
    return Arrays.stream(sampledStates)
        .map(nodeStateFunction)
        .collect(Collectors.toCollection(sampleSupplier));
  }

  public static <F extends E, E extends Collection<NodeState>> E getStateCollection(
      NodeState[] sampledStates, Supplier<F> sampleSupplier) {
    return getStateCollectionCommon(sampledStates, sampleSupplier, nodeState -> nodeState);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Collection<NodeState>, S extends T> T getSampleCollection(
      SampleData sampleData, Supplier<S> supplier) {
    try {
      return (S) sampleData.getStateCollection();
    } catch (ClassCastException e) {
      return rebuildStateCollection(sampleData, supplier);
    }
  }

  public static void setExportArrayFromNodes(
      Collection<Node> nodeCollection, SampleData sampleData) {
    if (nodeCollection.isEmpty()) {
      resetExportArray(sampleData);
      return;
    }
    Set<Node> nodes = new HashSet<>(nodeCollection);
    sampleData.setExportArray(
        Arrays.stream(sampleData.getRawArray())
            .filter(nodeState -> nodes.contains(nodeState.getNode()))
            .toArray(NodeState[]::new));
  }

  public static void resetExportArray(SampleData sampleData) {
    sampleData.setExportArray(Arrays.stream(sampleData.getRawArray()).toArray(NodeState[]::new));
  }

  public static <F extends E, E extends Collection<R>, R> E getStateIdCollection(
      NodeState[] sampledStates, Supplier<F> sampleSupplier, Class<R> idClass) {
    return getStateCollectionCommon(
        sampledStates, sampleSupplier, (nodeState -> idClass.cast(nodeState.getId())));
  }
}
