package io.github.alecredmond.internal.application.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;
import io.github.alecredmond.export.method.sampler.Sample;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@Data
public class LikelihoodWeightingSamplerData {
  private final Node[] nodes;
  private final TableHelper<?>[] tableHelpers;
  private Map<Node, NodeState> observations;
  private int numberOfSamples;
  private NodeState[] defaultSample;
  private Map<Set<NodeState>, Double> weightedStateSets;
  private Map<Sample, Double> weightedSamples;
  private Map<Sample, Integer> distributedSamples;

  public LikelihoodWeightingSamplerData(Node[] nodes, TableHelper<?>[] tableHelpers) {
    this.nodes = nodes;
    this.tableHelpers = tableHelpers;
  }
}
