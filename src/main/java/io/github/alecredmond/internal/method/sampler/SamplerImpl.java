package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.sampler.SampleCollection;
import io.github.alecredmond.export.method.sampler.Sampler;
import io.github.alecredmond.internal.method.network.NetworkDataUtils;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.io.Serializable;
import java.util.*;

public abstract class SamplerImpl implements Sampler {
  protected static final Random RANDOM = new Random();
  protected final BayesianNetworkData data;

  protected SamplerImpl(BayesianNetworkData data) {
    this.data = data;
  }

  @Override
  public SampleCollection generateSamplesById(
      Collection<Serializable> observedStateIDs, int numberOfSamples) {
    return generateSamples(NetworkDataUtils.getStatesByID(observedStateIDs, data), numberOfSamples);
  }

  @Override
  public SampleCollection generateSamples(
      Collection<NodeState> observedStates, int numberOfSamples) {
    if (numberOfSamples < 0) {
      throw new IllegalArgumentException("Attempted to generate a negative number of samples!");
    }
    return generateFromRequest(NodeUtils.generateRequest(observedStates), numberOfSamples);
  }

  protected abstract SampleCollection generateFromRequest(
      Map<Node, NodeState> observations, int numberOfSamples);

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
