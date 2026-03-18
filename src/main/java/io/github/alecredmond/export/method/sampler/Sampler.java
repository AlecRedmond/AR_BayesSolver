package io.github.alecredmond.export.method.sampler;

import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.sampler.LikelihoodWeightingSampler;

public interface Sampler {

  static Sampler create(InferenceEngine engine) {
    return new LikelihoodWeightingSampler(engine.getNetwork(), engine);
  }

  SampleCollection generateSamples(int numberOfSamples);

  BayesianNetwork getNetwork();

  InferenceEngine getInferenceEngine();
}
