package io.github.alecredmond.method.inference;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.method.inference.junctiontree.JTAInitializer;
import io.github.alecredmond.method.inference.junctiontree.JTASolver;
import io.github.alecredmond.method.inference.junctiontree.JunctionTreeAlgorithm;
import io.github.alecredmond.method.node.NodeUtils;
import io.github.alecredmond.method.sampler.export.SampleCollection;
import io.github.alecredmond.method.sampler.internal.LikelihoodWeightingSampler;
import io.github.alecredmond.method.sampler.internal.Sampler;
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
    Set<NodeState> currentEvidence = new HashSet<>(networkData.getObserved().values());
    double currentJointProb = junctionTree.getProbabilityOfEvidence();
    try {
      Set<NodeState> joinedEvidence = new HashSet<>(currentEvidence);
      joinedEvidence.addAll(newEvidence);
      observeNetwork(joinedEvidence);
      double newJointProb = junctionTree.getProbabilityOfEvidence();
      observeNetwork(currentEvidence);
      return currentJointProb == 0.0 ? 0.0 : newJointProb / currentJointProb;
    } catch (NodeStateConflictException e) {
      log.error("Nodes sharing the same state found in {}", NodeUtils.formatToString(newEvidence));
      return 0.0;
    }
  }

  public void observeNetwork(Collection<NodeState> observed) {
    if (!networkData.isSolved()) {
      return;
    }
    Map<Node, NodeState> observedMap = convertToMap(observed);
    junctionTree.observeNetwork(observedMap);
    junctionTree.writeObservations();
  }

  public Map<Node, NodeState> convertToMap(Collection<NodeState> evidenceStates) {
    NodeUtils.generateRequest(evidenceStates);
    Map<Node, NodeState> evidence = new HashMap<>();
    for (NodeState state : evidenceStates) {
      checkNoDuplicates(evidence, state);
      evidence.put(state.getNode(), state);
    }
    return evidence;
  }

  private void checkNoDuplicates(Map<Node, NodeState> evidence, NodeState state) {
    if (evidence.containsKey(state.getNode())) {
      throw new IllegalArgumentException("Tried to observe multiple NodeStates on the same node!");
    }
  }

  public void runSolver() {
    if (networkData.getNodeIDsMap().isEmpty()) {
      return;
    }
    JTASolver.solveNetwork(this);
    networkData.setSolved(true);
    junctionTree =
        new JunctionTreeAlgorithm(JTAInitializer.buildInferenceConfiguration(networkData));
    observeNetwork(new HashSet<>());
  }
}
