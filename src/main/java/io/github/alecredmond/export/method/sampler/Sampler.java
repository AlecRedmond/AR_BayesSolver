package io.github.alecredmond.export.method.sampler;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.sampler.LikelihoodWeightingSampler;
import java.io.Serializable;
import java.util.Collection;

public interface Sampler {

  static Sampler create(BayesianNetwork network) {
    return new LikelihoodWeightingSampler(network);
  }

  SampleCollection generateSamples(int numberOfSamples);

  SampleCollection generateSamples(InferenceEngine engine, int numberOfSamples);

  SampleCollection generateSamples(Collection<NodeState> evidence, int numberOfSamples);

  <T extends Serializable> SampleCollection generateSamplesById(
      Collection<T> evidenceStateIDs, int numberOfSamples);

  BayesianNetwork getNetwork();
}
