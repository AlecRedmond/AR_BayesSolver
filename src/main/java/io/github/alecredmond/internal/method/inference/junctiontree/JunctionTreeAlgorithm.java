package io.github.alecredmond.internal.method.inference.junctiontree;

import static io.github.alecredmond.internal.method.node.NodeUtils.*;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.inference.InferenceEngine.InferenceType;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;
import io.github.alecredmond.internal.application.inference.SolverConfigs;
import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.inference.junctiontree.Separator;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.JunctionTreeTableHelper;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class JunctionTreeAlgorithm {
  private final JunctionTreeData data;
  private final JTANetworkWriter networkWriter;

  public JunctionTreeAlgorithm(JunctionTreeData data) {
    this.data = data;
    this.networkWriter = new JTANetworkWriter(data);
    networkWriter.initializeJunctionTreeFromNetwork();
  }

  public static JunctionTreeAlgorithm buildForSolver(
      BayesianNetworkData bnd, SolverConfigs configs) {
    return new JunctionTreeAlgorithm(
        new JTADataBuilder().buildNewSolverConfiguration(bnd, configs));
  }

  public static JunctionTreeAlgorithm buildForInference(
      BayesianNetworkData bnd, InferenceType inferenceType) {
    return new JunctionTreeAlgorithm(
        new JTADataBuilder().buildNewInferenceConfiguration(bnd, inferenceType));
  }

  public void rebuildJTA(BayesianNetworkData bnd, InferenceType inferenceType) {
    new JTADataBuilder().buildInferenceConfiguration(data, bnd, inferenceType);
    networkWriter.initializeJunctionTreeFromNetwork();
  }

  public void writeObservations() {
    networkWriter.writeObservations();
  }

  public void observeNetwork(Map<Node, NodeState> observed) {
    if (data.getNetworkData().getNodes().isEmpty()) {
      return;
    }
    data.setObservedEvidence(observed);
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
        .map(JunctionTreeTable::getHelper)
        .forEach(TableHelper::marginalizeTable);
    Arrays.stream(data.getSeparators()).forEach(Separator::resetSeparator);
  }

  public void writeTablesToNetwork() {
    networkWriter.writeBackToCPTs();
  }

  public double getJointProbability() {
    return data.getJointProbability();
  }

  public double getJointProbOfMeasured(Collection<NodeState> newEvidence) {
    double cliqueSums = multiplyTableSums(data.getCliques(), Clique::getTable, newEvidence);
    double separatorSums =
        multiplyTableSums(data.getSeparators(), Separator::getTable, newEvidence);
    return separatorSums == 0.0 ? 0.0 : cliqueSums / separatorSums;
  }

  public void sumTransfer(Clique clique) {
    distributeEvidence(clique, new HashSet<>());
  }

  private <T> double multiplyTableSums(
      T[] array, Function<T, JunctionTreeTable> tableFunction, Collection<NodeState> newEvidence) {
    return Arrays.stream(array)
        .map(tableFunction)
        .map(JunctionTreeTable::getHelper)
        .mapToDouble(helper -> helper.sumProbabilities(newEvidence))
        .reduce(1.0, (x, y) -> x * y);
  }

  private void setJointProbability() {
    data.setJointProbability(getJointProbOfMeasured(new HashSet<>()));
  }

  private void resetObservations() {
    Arrays.stream(data.getCliques())
        .map(Clique::getHandler)
        .forEach(JunctionTreeTableHelper::resetObservations);
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
