package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.sampler.SampleCollection;
import io.github.alecredmond.export.method.sampler.Sampler;
import io.github.alecredmond.internal.method.network.NetworkDataUtils;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.io.Serializable;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class SamplerImpl implements Sampler {
  protected static final Random RANDOM = new Random();
  protected final BayesianNetworkData data;
  protected final InferenceEngine engine;

  protected SamplerImpl(BayesianNetwork network) {
    this.data = network.getNetworkData();
    this.engine = InferenceEngine.create(network);
  }

  protected SamplerImpl(BayesianNetwork network, InferenceEngine engine) {
    this.data = network.getNetworkData();
    this.engine = engine;
  }

  @Override
  public SampleCollection generateSamplesById(
      Collection<Serializable> observedStateIDs, int numberOfSamples) {
    return generateSamples(NetworkDataUtils.getStatesByID(observedStateIDs, data), numberOfSamples);
  }

  @Override
  public SampleCollection generateSamples(
      Collection<NodeState> observedStates, int numberOfSamples) {
    return validityChecker(observedStates, numberOfSamples)
        .map(validated -> generateSamplesIfValid(validated, numberOfSamples))
        .orElse(null);
  }

  private Optional<Map<Node, NodeState>> validityChecker(
      Collection<NodeState> states, int numberOfSamples) {
    if (numberOfSamples < 1) {
      log.error("Attempted to create fewer than 0 samples!");
      return Optional.empty();
    }
    try {
      return Optional.of(NodeUtils.generateRequest(states, engine.getCurrentObservations().values()));
    } catch (NodeStateConflictException e) {
      log.error(e.getMessage());
      return Optional.empty();
    }
  }

  protected abstract SampleCollection generateSamplesIfValid(
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
  }  public SampleCollection generateSamples(Map<Node, NodeState> observations, int numberOfSamples) {
    return generateSamples(observations.values(), numberOfSamples);
  }

  @Override
  public SampleCollection generateSamples(int numberOfSamples) {
    return generateSamples(engine.getCurrentObservations(), numberOfSamples);
  }


}
