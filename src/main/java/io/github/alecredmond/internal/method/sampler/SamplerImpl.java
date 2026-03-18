package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.sampler.SampleCollection;
import io.github.alecredmond.export.method.sampler.Sampler;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class SamplerImpl implements Sampler {
  protected static final Random RANDOM = new Random();
  protected final BayesianNetworkData data;
  protected final InferenceEngine engine;

  protected SamplerImpl(BayesianNetwork network, InferenceEngine engine) {
    this.data = network.getNetworkData();
    this.engine = engine;
  }

  @Override
  public SampleCollection generateSamples(int numberOfSamples) {
    return generateWithEvidence(engine.getCurrentObservations(), numberOfSamples);
  }

  protected abstract SampleCollection generateWithEvidence(
      Map<Node, NodeState> observations, int numberOfSamples);

  public BayesianNetwork getNetwork() {
    return engine.getNetwork();
  }

  public InferenceEngine getInferenceEngine() {
    return engine;
  }

  protected <R, E extends Number> R nextRandom(Map<R, E> weights) {
    if (weights.isEmpty()) {
      return null;
    }

    double totalWeight = weights.values().stream().mapToDouble(Number::doubleValue).sum();
    double randomValue = RANDOM.nextDouble() * totalWeight;

    for (Map.Entry<R, E> entry : weights.entrySet()) {
      randomValue -= entry.getValue().doubleValue();
      if (randomValue <= 0.0) return entry.getKey();
    }
    return null;
  }
}
