package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.sampler.Sampler;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.io.Serializable;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class SamplerImpl implements Sampler {
  protected static final Random RANDOM = new Random();
  protected final BayesianNetwork network;

  protected SamplerImpl(BayesianNetwork network) {
    this.network = network;
  }

  @Override
  public SampleCollectionImpl generateSamples(int numberOfSamples) {
    return generateSamples(new HashMap<>(), numberOfSamples);
  }

  protected abstract SampleCollectionImpl generateSamples(
      Map<Node, NodeState> observations, int numberOfSamples);

  public SampleCollectionImpl generateSamples(InferenceEngine engine, int numberOfSamples) {
    return generateSamples(engine.getCurrentObservations(), numberOfSamples);
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

  @Override
  public SampleCollectionImpl generateSamples(
      Collection<NodeState> observedStates, int numberOfSamples) {
    try {
      return generateSamples(
          NodeUtils.generateOrderedRequest(observedStates, network.getNetworkData().getNodes()),
          numberOfSamples);
    } catch (NodeStateConflictException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  @Override
  public <T extends Serializable> SampleCollectionImpl generateSamplesById(
      Collection<T> observedStateIds, int numberOfSamples) {
    return generateSamples(network.getNodeStates(observedStateIds), numberOfSamples);
  }
}
