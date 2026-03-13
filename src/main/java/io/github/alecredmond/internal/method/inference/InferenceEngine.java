package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.sampler.SampleCollection;
import io.github.alecredmond.internal.method.inference.junctiontree.JTASolver;
import io.github.alecredmond.internal.method.inference.junctiontree.JunctionTreeAlgorithm;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.sampler.LikelihoodWeightingSampler;
import io.github.alecredmond.internal.method.sampler.Sampler;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class InferenceEngine {
  private final BayesianNetworkData networkData;
  private JunctionTreeAlgorithm junctionTree;

  public InferenceEngine(BayesianNetworkData networkData) {
    this.networkData = networkData;
    this.junctionTree = null;
  }

  public SampleCollection generateSamples(Map<Node, NodeState> observations, int numberOfSamples) {
    if (numberOfSamples < 0) {
      throw new IllegalArgumentException("Attempted to generate a negative number of samples!");
    }
    return getSampler().generateSamples(observations, numberOfSamples);
  }

  private Sampler getSampler() {
    // Currently only Likelihood Weighting Sampler is used, this may change in the future
    return new LikelihoodWeightingSampler(networkData);
  }

  public double getProbabilityFromCurrentObservations(Set<NodeState> newEvidence) {
    try {
      double currentJointProb = junctionTree.getJointProbability();
      if (currentJointProb == 0.0) return 0.0;
      double newJointProb = junctionTree.getJointProbOfNewEvidence(newEvidence);
      return newJointProb / currentJointProb;
    } catch (NodeStateConflictException e) {
      log.warn("Conflicting states found in {}", NodeUtils.formatStatesToString(newEvidence));
      return 0.0;
    }
  }

  public void runSolver() {
    if (networkData.getNodeIDsMap().isEmpty()) {
      return;
    }
    JTASolver.solveNetwork(this);
    networkData.setSolved(true);
    junctionTree = JunctionTreeAlgorithm.buildForInference(networkData);
    observeNetwork(new HashSet<>());
  }

  public void observeNetwork(Collection<NodeState> observed) {
    if (!networkData.isSolved()) {
      return;
    }
    junctionTree.observeNetwork(NodeUtils.generateRequest(observed));
    junctionTree.writeObservations();
  }
}
