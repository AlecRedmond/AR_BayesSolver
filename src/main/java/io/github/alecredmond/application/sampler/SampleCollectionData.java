package io.github.alecredmond.application.sampler;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import io.github.alecredmond.method.sampler.export.Sample;
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
