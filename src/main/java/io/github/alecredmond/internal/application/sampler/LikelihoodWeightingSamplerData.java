package io.github.alecredmond.internal.application.sampler;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTableQueryTool;
import io.github.alecredmond.internal.method.sampler.SampleImpl;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@Data
public class LikelihoodWeightingSamplerData {
  private final Node[] nodes;
  private final NetworkTableQueryTool[] tableHelpers;
  private Map<Node, NodeState> observations;
  private int numberOfSamples;
  private NodeState[] defaultSample;
  private Map<Set<NodeState>, Double> weightedStateSets;
  private Map<SampleImpl, Double> weightedSamples;
  private Map<SampleImpl, Integer> distributedSamples;

  public LikelihoodWeightingSamplerData(Node[] nodes, NetworkTableQueryTool[] tableHelpers) {
    this.nodes = nodes;
    this.tableHelpers = tableHelpers;
  }
}
