package io.github.alecredmond.method.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.sampler.SampleCollection;
import io.github.alecredmond.export.method.sampler.Sampler;
import java.util.Set;
import lombok.Data;

@Data
public class SamplePackage {
  private final SampleCollection test;
  private final BayesianNetwork network;
  private final InferenceEngine engine;
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
    this.network = network;
    this.engine = InferenceEngine.create(network).observeNetworkFromIds(observedStateIds);
    this.test = Sampler.create(engine).generateSamples(numberOfSamples);
    this.numberOfSamples = numberOfSamples;
    this.observedStateIds = observedStateIds;
    this.observedStates = network.getNodeStates(observedStateIds);
    this.exportNodeIds = exportNodeIds;
    this.exportNodes = network.getNodes(exportNodeIds);
    this.measuredStateIds = measuredStateIds;
    this.measuredStates = network.getNodeStates(measuredStateIds);
  }
}
