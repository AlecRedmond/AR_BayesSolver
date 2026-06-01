package io.github.alecredmond.internal.method.inference;

import static io.github.alecredmond.export.method.inference.InferenceEngine.InferenceType.JOINT_TABLE_INFERENCE;
import static io.github.alecredmond.export.method.inference.InferenceEngine.InferenceType.JUNCTION_TREE_INFERENCE;
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
    if (!attemptSolve(network)) {
      log.error("Could not build an inference engine!");
      return null;
    }
    InferenceType inferenceType =
        new PropertiesLoader().loadBoolean(INFERENCE_USE_JTA_INFERENCE)
            ? JUNCTION_TREE_INFERENCE
            : JOINT_TABLE_INFERENCE;
    JunctionTreeAlgorithm jta =
        JunctionTreeAlgorithm.buildForInference(network.getNetworkData(), inferenceType);
    return new InferenceEngineImpl(network, jta, inferenceType);
  }

  private boolean attemptSolve(BayesianNetwork network) {
    if (network.isSolved()) return true;
    log.warn(
        "Attempted to create an Inference Engine on unsolved network {}. Will now attempt to solve...",
        network.getNetworkData().getNetworkName());
    return BayesSolver.create(network).solve();
  }
}
