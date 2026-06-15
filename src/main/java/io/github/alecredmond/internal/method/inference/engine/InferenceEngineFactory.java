package io.github.alecredmond.internal.method.inference.engine;

import static io.github.alecredmond.export.method.inference.InferenceEngine.InferenceType.SINGLE_TABLE_ALGORITHM;
import static io.github.alecredmond.export.method.inference.InferenceEngine.InferenceType.JUNCTION_TREE_ALGORITHM;
import static io.github.alecredmond.internal.method.utils.AppProperty.INFERENCE_USE_JTA_INFERENCE;

import io.github.alecredmond.export.method.inference.BayesSolver;
import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.inference.InferenceEngine.InferenceType;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.internal.method.inference.junctiontree.JunctionTreeAlgorithm;
import io.github.alecredmond.internal.method.utils.PropertiesLoader;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public class InferenceEngineFactory {

  public InferenceEngine create(BayesianNetwork network) {
    InferenceType inferenceType =
        new PropertiesLoader().loadBoolean(INFERENCE_USE_JTA_INFERENCE)
            ? JUNCTION_TREE_ALGORITHM
            : SINGLE_TABLE_ALGORITHM;
    return create(network, inferenceType);
  }

  public InferenceEngine create(BayesianNetwork network, InferenceType inferenceType) {

    BayesSolver solver = BayesSolver.create(network);
    if (!attemptSolve(network, solver)) {
      log.error("Could not build an inference engine!");
      return null;
    }
    return new InferenceEngineImpl(
        network,
        solver,
        JunctionTreeAlgorithm.buildForInference(network.getNetworkData(), inferenceType),
        inferenceType);
  }

  private boolean attemptSolve(BayesianNetwork network, BayesSolver solver) {
    if (network.isSolved()) return true;
    log.warn(
        "Attempted to create an Inference Engine on unsolved network '{}'. Will now attempt to solve...",
        network.getNetworkData().getNetworkName());
    return solver.forceSolve();
  }
}
