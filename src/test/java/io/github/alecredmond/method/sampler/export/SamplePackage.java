package io.github.alecredmond.method.sampler.export;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.method.network.export.BayesianNetwork;
import java.util.Set;
import lombok.Data;

@Data
public class SamplePackage {
  private final SampleCollection test;
  private final BayesianNetwork network;
  private final int numberOfSamples;
  private final Set<String> observedStateIds;
  private final Set<NodeState> observedStates;
  private final Set<String> exportNodeIds;
  private final Set<Node> exportNodes;
  private final Set<String> measuredStateIds;
  private final Set<NodeState> measuredStates;

  public SamplePackage(
      BayesianNetwork network,
      int numberOfSamples,
      Set<String> observedStateIds,
      Set<String> exportNodeIds,
      Set<String> measuredStateIds) {
    this.test = network.observeNetwork(observedStateIds).generateSamples(numberOfSamples);
    this.network = network;
    this.numberOfSamples = numberOfSamples;
    this.observedStateIds = observedStateIds;
    this.observedStates = network.getNodeStates(observedStateIds);
    this.exportNodeIds = exportNodeIds;
    this.exportNodes = network.getNodes(exportNodeIds);
    this.measuredStateIds = measuredStateIds;
    this.measuredStates = network.getNodeStates(measuredStateIds);
  }
}
