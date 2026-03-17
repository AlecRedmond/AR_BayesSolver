package io.github.alecredmond.method.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import java.util.Set;

import io.github.alecredmond.export.method.sampler.SampleCollection;
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
  private final boolean printMarginals;

  public SamplePackage(
      BayesianNetwork network,
      int numberOfSamples,
      Set<String> observedStateIds,
      Set<String> exportNodeIds,
      Set<String> measuredStateIds,
      boolean printMarginals) {
    this.printMarginals = printMarginals;
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
