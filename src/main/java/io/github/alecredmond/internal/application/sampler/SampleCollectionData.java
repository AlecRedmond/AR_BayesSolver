package io.github.alecredmond.internal.application.sampler;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.sampler.Sample;
import java.util.*;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SampleCollectionData {
  private final int totalSamples;
  private final List<Sample> samples;
  private final Map<Node, NodeState> networkObservations;
  private final Node[] nodes;
}
