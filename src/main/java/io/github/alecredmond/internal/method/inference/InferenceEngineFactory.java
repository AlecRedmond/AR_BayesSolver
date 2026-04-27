package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.export.method.inference.BayesSolver;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.inference.junctiontree.JunctionTreeAlgorithm;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public class InferenceEngineFactory {

  public InferenceEngine create(BayesianNetwork network) {
    if (!checkSolved(network)) {
      log.error("Could not build an inference engine!");
      return null;
    }
    return new InferenceEngineImpl(
        network, JunctionTreeAlgorithm.buildForInference(network.getNetworkData()));
  }

  private boolean checkSolved(BayesianNetwork network) {
    if (network.getNetworkData().isSolved()) return true;
    log.warn(
        "Attempted to create an Inference Engine on unsolved network {}. Will now attempt to solve...",
        network.getNetworkData().getNetworkName());
    return BayesSolver.create(network).solve();
  }
}
