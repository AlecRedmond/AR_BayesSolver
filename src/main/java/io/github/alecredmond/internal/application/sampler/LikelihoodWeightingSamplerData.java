package io.github.alecredmond.internal.application.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.probabilitytables.NetworkTableHelper;
import io.github.alecredmond.internal.method.sampler.SampleImpl;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@SuppressWarnings("rawtypes")
@Data
public class LikelihoodWeightingSamplerData {
  private final Node[] nodes;
  private final NetworkTableHelper[] tableHelpers;
  private Map<Node, NodeState> observations;
  private int numberOfSamples;
  private NodeState[] defaultSample;
  private Map<Set<NodeState>, Double> weightedStateSets;
  private Map<SampleImpl, Double> weightedSamples;
  private Map<SampleImpl, Integer> distributedSamples;

  public LikelihoodWeightingSamplerData(Node[] nodes, NetworkTableHelper[] tableHelpers) {
    this.nodes = nodes;
    this.tableHelpers = tableHelpers;
  }
}
