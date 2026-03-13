package io.github.alecredmond.internal.method.inference.junctiontree;

import static io.github.alecredmond.internal.method.node.NodeUtils.*;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.inference.junctiontree.Separator;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class JunctionTreeAlgorithm {
  private final JunctionTreeData data;

  public JunctionTreeAlgorithm(JunctionTreeData data) {
    this.data = data;
    JTANetworkWriter.initializeJunctionTreeFromNetwork(data);
  }

  public static JunctionTreeAlgorithm buildForSolver(BayesianNetworkData bnd) {
    return new JunctionTreeAlgorithm(JTAInitializer.buildSolverConfiguration(bnd));
  }

  public static JunctionTreeAlgorithm buildForInference(BayesianNetworkData bnd) {
    return new JunctionTreeAlgorithm(JTAInitializer.buildInferenceConfiguration(bnd));
  }

  public void writeObservations() {
    JTANetworkWriter.writeObservations(data);
  }

  public void observeNetwork(Map<Node, NodeState> observed) {
    if (data.getNodes().isEmpty()) {
      return;
    }
    data.setObserved(observed);
    resetObservations();
    if (observed.isEmpty()) {
      passMessages(data.getCliques()[0]);
      setJointProbability();
      return;
    }
    Map<Node, NodeState> observedLeft = new HashMap<>(observed);
    Set<NodeState> evidenceStates = new HashSet<>();
    Set<Node> overlap = new HashSet<>();
    while (!observedLeft.isEmpty()) {
      Clique largestOverlap = findLargestOverlap(observedLeft, overlap);
      popFromMapIntoEvidenceStates(overlap, observedLeft, evidenceStates);
      largestOverlap.getHandler().setObserved(evidenceStates);
      passMessages(largestOverlap);
    }
    setJointProbability();
  }

  public void marginalizeTables() {
    Arrays.stream(data.getCliques())
        .map(Clique::getTable)
        .forEach(ProbabilityTable::marginalizeTable);
    Arrays.stream(data.getSeparators()).forEach(Separator::resetSeparator);
  }

  public void writeTablesToNetwork() {
    JTANetworkWriter.writeToNetwork(data);
  }

  public double getJointProbability() {
    return data.getJointProbability();
  }

  public double getJointProbOfNewEvidence(Collection<NodeState> newEvidence) {
    Map<Node, NodeState> request = createNewEvidenceRequest(newEvidence);
    return multiplyTableSums(data.getCliques(), Clique::getTable, request)
        / multiplyTableSums(data.getSeparators(), Separator::getTable, request);
  }

  private <T> double multiplyTableSums(
      T[] array, Function<T, ProbabilityTable> tableFunction, Map<Node, NodeState> request) {
    return Arrays.stream(array)
        .map(tableFunction)
        .mapToDouble(pt -> TableUtils.sumProbabilities(request, pt))
        .reduce(1.0, (x, y) -> x * y);
  }

  private void setJointProbability() {
    data.setJointProbability(getJointProbOfNewEvidence(new HashSet<>()));
  }

  private Map<Node, NodeState> createNewEvidenceRequest(Collection<NodeState> newEvidence) {
    if (newEvidence.isEmpty()) {
      return new HashMap<>();
    }
    Map<Node, NodeState> observed = data.getObservedEvidence();
    Map<Node, NodeState> request = generateRequest(newEvidence, observed.values());
    observed.keySet().forEach(request::remove);
    return request;
  }

  private void resetObservations() {
    Arrays.stream(data.getCliques())
        .map(Clique::getHandler)
        .forEach(JTATableHandler::resetObservations);
    Arrays.stream(data.getSeparators()).forEach(Separator::resetSeparator);
  }

  private void popFromMapIntoEvidenceStates(
      Set<Node> overlap, Map<Node, NodeState> observedLeft, Set<NodeState> evidenceStates) {
    evidenceStates.clear();
    overlap.forEach(
        node -> {
          evidenceStates.add(observedLeft.get(node));
          observedLeft.remove(node);
        });
  }

  private Clique findLargestOverlap(Map<Node, NodeState> observedLeft, Set<Node> overlap) {
    overlap.clear();

    Map.Entry<Clique, Set<Node>> largestOverlapWithNodes =
        Arrays.stream(data.getCliques())
            .map(clique -> Map.entry(clique, getOverlap(clique.getNodes(), observedLeft.keySet())))
            .filter(entry -> !entry.getValue().isEmpty())
            .max(Comparator.comparingInt(entry -> entry.getValue().size()))
            .orElseThrow();

    overlap.addAll(largestOverlapWithNodes.getValue());
    return largestOverlapWithNodes.getKey();
  }

  void sumTransfer(Clique clique) {
    distributeEvidence(clique, new HashSet<>());
  }

  private void passMessages(Clique clique) {
    collectEvidence(clique, new HashSet<>());
    distributeEvidence(clique, new HashSet<>());
  }

  private void collectEvidence(Clique currentClique, Set<Clique> visited) {
    visited.add(currentClique);
    getNextSeparators(currentClique, visited)
        .forEach(
            (nextClique, separator) -> {
              collectEvidence(nextClique, visited);
              separator.run(nextClique);
            });
  }

  private void distributeEvidence(Clique currentClique, Set<Clique> visited) {
    visited.add(currentClique);
    getNextSeparators(currentClique, visited)
        .forEach(
            (nextClique, separator) -> {
              separator.run(currentClique);
              distributeEvidence(nextClique, visited);
            });
  }

  private Map<Clique, Separator> getNextSeparators(Clique currentClique, Set<Clique> cliqueChain) {
    return currentClique.getSeparatorMap().entrySet().stream()
        .filter(entry -> !cliqueChain.contains(entry.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
