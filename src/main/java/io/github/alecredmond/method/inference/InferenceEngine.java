package io.github.alecredmond.method.inference;

import static io.github.alecredmond.application.inference.SampleGeneratorType.*;

import io.github.alecredmond.application.inference.InferenceEngineConfigs;
import io.github.alecredmond.application.inference.SampleGeneratorType;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.method.inference.junctiontree.JTAInitializer;
import io.github.alecredmond.method.inference.junctiontree.JTASolver;
import io.github.alecredmond.method.inference.junctiontree.JunctionTreeAlgorithm;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
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

    return getSampler(tClass)
        .generateSamples(
            networkData, networkData.getObserved(), excludeNodes, includeNodes, numberOfSamples);
  }

  private <T> Sampler<T> getSampler(Class<T> tClass) {
    SampleGeneratorType type = Objects.requireNonNull(configs.getSampleGenerator());
    if (type == LIKELIHOOD_WEIGHTING_SAMPLER) {
      return new LikelihoodWeightingSampler<>(tClass);
    }
    throw new IllegalStateException("Unexpected value: " + type);
  }

  public double getProbabilityFromCurrentObservations(Set<NodeState> newEvidence) {
    if (conflictingEvidence(newEvidence)) return 0;
    if (newEvidence.isEmpty()) return 1.0;

    Map<Node, NodeState> currentObservations = networkData.getObserved();
    Map<Node, NodeState> newObservations = convertToMap(newEvidence);
    newObservations.putAll(currentObservations);
    if (currentObservations.equals(newObservations)) return 1.0; //TODO - Hit Branch In Test Suite

    if (junctionTree.isMarginalized()) junctionTree.observeNetwork(currentObservations); //TODO - Hit Branch In Test Suite
    double jointProbWithCurrentEvidence = junctionTree.getProbabilityOfEvidence();
    if (jointProbWithCurrentEvidence == 0) return 0; //TODO - Hit Branch In Test Suite

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
      // Duplicate keys detected
      return true;
    }

    if (newEvidence.size() != evidenceStates.size()) return true; //TODO - Hit Branch In Test Suite

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
      return !newEvents.get(n).equals(s); //TODO - Hit Branch In Test Suite
    }
    return false;
  }

  private void checkNoDuplicates(Map<Node, NodeState> evidence, NodeState state) {
    if (evidence.containsKey(state.getNode())) {
      throw new IllegalArgumentException("Tried to observe multiple NodeStates on the same node!");
    }
  }

  public void observeNetwork(Collection<NodeState> observed) {
    if (!networkData.isSolved()) runSolver(); //TODO - Hit Branch In Test Suite
    Map<Node, NodeState> observedMap = convertToMap(observed);
    junctionTree.observeNetwork(observedMap);
    junctionTree.marginalizeTables();
    junctionTree.writeObservations();
  }

  public void runSolver() {
    JTASolver.solveNetwork(this);
    networkData.setSolved(true);
    junctionTree =
        new JunctionTreeAlgorithm(JTAInitializer.buildInferenceConfiguration(networkData));
  }
}
