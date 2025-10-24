package io.github.alecredmond.method.sampler;

import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.solver.InferenceEngineConfigs;
import io.github.alecredmond.method.sampler.jtasampler.JTAConstraintSolver;
import io.github.alecredmond.method.sampler.jtasampler.JunctionTreeAlgorithm;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InferenceEngine {
  private final BayesianNetworkData networkData;
  private final InferenceEngineConfigs configs;
  private JunctionTreeAlgorithm junctionTree;

  public InferenceEngine(BayesianNetworkData networkData, InferenceEngineConfigs configs) {
    this.networkData = networkData;
    this.configs = configs;
    this.junctionTree = null;
  }

  public <T> List<List<T>> generateSamples(
      Set<Node> excludeNodes, Set<Node> includeNodes, int numberOfSamples, Class<T> tClass) {
    if (numberOfSamples < 0) {
      throw new IllegalArgumentException("Attempted to generate < 0 samples!");
    }
    if (includeNodes.isEmpty()) {
      includeNodes = new HashSet<>(networkData.getNodes());
      includeNodes.removeAll(excludeNodes);
    }
    return new LikelihoodWeightingSampler<>(networkData, tClass)
        .generateSamples(
            networkData.getObservedStatesMap(), excludeNodes, includeNodes, numberOfSamples);
  }

  public double getProbabilityFromCurrentObservations(Set<NodeState> newEvidence) {
    if (!networkData.isSolved()) runSolver();
    if (conflictingEvidence(newEvidence)) return 0;
    if (newEvidence.isEmpty()) return 1.0;

    Map<Node, NodeState> currentObservations = networkData.getObservedStatesMap();
    Map<Node, NodeState> extraObservations = convertToMap(newEvidence);
    extraObservations.putAll(currentObservations);
    if (currentObservations.equals(extraObservations)) return 1.0;

    if (junctionTree.isMarginalized()) junctionTree.observeNetwork(currentObservations);
    double jointProbWithCurrentEvidence = junctionTree.getProbabilityOfEvidence();
    if (jointProbWithCurrentEvidence == 0) return 0;

    junctionTree.observeNetwork(extraObservations);
    double jointProbWithExtraEvidence = junctionTree.getProbabilityOfEvidence();

    double probability = jointProbWithExtraEvidence / jointProbWithCurrentEvidence;
    junctionTree.observeNetwork(currentObservations);
    return probability;
  }

  public void runSolver() {
    JTAConstraintSolver.solveNetwork(networkData, configs);
    networkData.setSolved(true);
    junctionTree = new JunctionTreeAlgorithm(networkData);
  }

  private boolean conflictingEvidence(Set<NodeState> evidenceStates) {
    Map<Node, NodeState> newEvidence;
    try {
      newEvidence =
          evidenceStates.stream()
              .map(state -> Map.entry(state.getNode(), state))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    } catch (IllegalStateException e) {
      // Duplicate keys detected
      return true;
    }

    if (newEvidence.size() != evidenceStates.size()) return true;

    return networkData.getObservedStatesMap().entrySet().stream()
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

  public void observeNetwork(Collection<NodeState> observed) {
    if (!networkData.isSolved()) runSolver();
    Map<Node, NodeState> observedMap = convertToMap(observed);
    junctionTree.observeNetwork(observedMap);
    junctionTree.marginalizeTables();
    junctionTree.writeObservations();
  }
}
