package io.github.alecredmond.export.method.sampler;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.sampler.LikelihoodWeightingSampler;
import java.io.Serializable;
import java.util.Collection;

public interface Sampler {

  static Sampler create(BayesianNetwork network) {
    return new LikelihoodWeightingSampler(network.getNetworkData());
  }

  SampleCollection generateSamplesById(
      Collection<Serializable> observedStateIDs, int numberOfSamples);

  SampleCollection generateSamples(Collection<NodeState> observedStates, int numberOfSamples);
}
