package io.github.alecredmond.method.inference;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.method.sampler.export.SampleCollection;
import io.github.alecredmond.method.inference.junctiontree.JTAInitializer;
import io.github.alecredmond.method.inference.junctiontree.JTASolver;
import io.github.alecredmond.method.inference.junctiontree.JunctionTreeAlgorithm;
import io.github.alecredmond.method.sampler.internal.LikelihoodWeightingSampler;
import io.github.alecredmond.method.sampler.internal.Sampler;
import java.util.*;
import java.util.stream.Collectors;

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
    if (conflictingEvidence(newEvidence)) return 0;
    if (newEvidence.isEmpty()) return 1.0;

    Map<Node, NodeState> currentObservations = networkData.getObserved();
    Map<Node, NodeState> newObservations = convertToMap(newEvidence);
    newObservations.putAll(currentObservations);
    if (currentObservations.equals(newObservations)) return 1.0;

    double jointProbWithCurrentEvidence = junctionTree.getProbabilityOfEvidence();
    if (jointProbWithCurrentEvidence == 0) return 0;

    junctionTree.observeNetwork(newObservations);
    double jointProbWithExtraEvidence = junctionTree.getProbabilityOfEvidence();

    double probability = jointProbWithExtraEvidence / jointProbWithCurrentEvidence;
    junctionTree.observeNetwork(currentObservations);
    return probability;
  }

  private boolean conflictingEvidence(Set<NodeState> evidenceStates) {
    Map<Node, NodeState> newEvidence;
    try {
      newEvidence =
          evidenceStates.stream()
              .map(state -> Map.entry(state.getNode(), state))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    } catch (IllegalStateException e) {
      // Duplicate keys detected, i.e. multiple states with same node
      return true;
    }

    if (newEvidence.size() != evidenceStates.size()) return true;

    return networkData.getObserved().entrySet().stream()
        .anyMatch(observedEntry -> sameKeyDifferentValue(observedEntry, newEvidence));
  }

  public Map<Node, NodeState> convertToMap(Collection<NodeState> evidenceStates) {
    Map<Node, NodeState> evidence = new HashMap<>();
    for (NodeState state : evidenceStates) {
      checkNoDuplicates(evidence, state);
      evidence.put(state.getNode(), state);
    }
    return evidence;
  }

  private boolean sameKeyDifferentValue(
      Map.Entry<Node, NodeState> observedEntry, Map<Node, NodeState> newEvents) {
    Node n = observedEntry.getKey();
    NodeState s = observedEntry.getValue();
    if (newEvents.containsKey(n)) {
      return !newEvents.get(n).equals(s);
    }
    return false;
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

  public void observeNetwork(Collection<NodeState> observed) {
    if (!networkData.isSolved()) {
      return;
    }
    Map<Node, NodeState> observedMap = convertToMap(observed);
    junctionTree.observeNetwork(observedMap);
    junctionTree.writeObservations();
  }
}
