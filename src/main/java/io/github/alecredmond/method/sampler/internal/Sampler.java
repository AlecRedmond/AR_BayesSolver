package io.github.alecredmond.method.sampler.internal;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.method.sampler.export.SampleCollection;
import java.util.*;

public abstract class Sampler {
  protected static final Random RANDOM = new Random();
  protected final BayesianNetworkData data;

  protected Sampler(BayesianNetworkData data) {
    this.data = data;
  }

  public abstract SampleCollection generateSamples(
      Map<Node, NodeState> observations, int numberOfSamples);

  protected <R, E extends Number> R nextRandom(Map<R, E> weights) {
    if (weights.isEmpty()) {
      throw new IllegalArgumentException("nextRandom received an empty weights map!");
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
