package io.github.alecredmond.export.application.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import io.github.alecredmond.export.method.sampler.Sample;
import lombok.Data;

@Data
public class SampleCollectionData {
  private final int totalSamples;
  private final Map<Sample, Integer> sampleMap;
  private final Map<Node, NodeState> networkObservations;
  private final Node[] nodes;
  private Supplier<? extends Collection<NodeState>> sampleSupplier;

  public SampleCollectionData(
      int totalSamples,
      Map<Sample, Integer> sampleMap,
      Map<Node, NodeState> networkObservations,
      Node[] nodes) {
    this.totalSamples = totalSamples;
    this.sampleMap = sampleMap;
    this.networkObservations = networkObservations;
    this.nodes = nodes;
    this.sampleSupplier = ArrayList::new;
  }
}
