package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.sampler.SampleData;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SampleUtils {
  private SampleUtils() {}

  @SuppressWarnings("unchecked")
  public static <T extends Collection<NodeState>, S extends T> T getSampleCollection(
      SampleData sampleData, Supplier<S> supplier) {
    if (sampleData.getSupplier().equals(supplier)) {
      return (T) sampleData.getStateCollection();
    }
    return rebuildStateCollection(sampleData, supplier);
  }

  public static <T extends Collection<NodeState>, R extends T> T rebuildStateCollection(
      SampleData sampleData, Supplier<R> supplier) {
    T newCollection = buildStatesAsCollection(sampleData.getExportStateArray(), supplier);
    sampleData.setStateCollection(newCollection);
    sampleData.setSupplier(supplier);
    return newCollection;
  }

  public static <F extends E, E extends Collection<NodeState>> E buildStatesAsCollection(
      NodeState[] sampledStates, Supplier<F> sampleSupplier) {
    return Arrays.stream(sampledStates).collect(Collectors.toCollection(sampleSupplier));
  }

  public static void setExportArrayFromNodes(
      Collection<Node> nodeCollection, SampleData sampleData) {
    if (nodeCollection.isEmpty()) {
      resetExportArray(sampleData);
      return;
    }
    Set<Node> nodes = new HashSet<>(nodeCollection);
    sampleData.setExportStateArray(
        Arrays.stream(sampleData.getRawStateArray())
            .filter(nodeState -> nodes.contains(nodeState.getNode()))
            .toArray(NodeState[]::new));
  }

  public static void resetExportArray(SampleData sampleData) {
    sampleData.setExportStateArray(
        Arrays.stream(sampleData.getRawStateArray()).toArray(NodeState[]::new));
  }
}
