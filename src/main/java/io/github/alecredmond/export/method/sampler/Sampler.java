package io.github.alecredmond.export.method.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.sampler.LikelihoodWeightingSampler;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface Sampler {

  static Sampler create(BayesianNetwork network) {
    return new LikelihoodWeightingSampler(network.getNetworkData());
  }

  SampleCollection generateSamplesById(
      Collection<Serializable> observedStateIDs, int numberOfSamples);

  SampleCollection generateSamples(Collection<NodeState> observedStates, int numberOfSamples);

  SampleCollection generateSamples(Map<Node,NodeState> evidence, int numberOfSamples);

  SampleCollection generateSamples(int numberOfSamples);
}
