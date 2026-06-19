package io.github.alecredmond.internal.method.inference.junctiontree;

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
import io.github.alecredmond.internal.method.node.NodeUtils;
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
    data.setObservedEvidence(observed);
    resetObservations();
    if (observed.isEmpty()) passMessages(data.getCliques()[0]);
    else applyObservations(observed);
    double jointProb = getJointProbOfMeasured(new HashSet<>());
    data.setJointProbability(jointProb);
  }

  public void normalizeTables() {
    Arrays.stream(data.getCliques())
        .map(Clique::getTable)
        .map(JunctionTreeTable::getHelper)
        .forEach(TableHelper::normalizeTable);
    Arrays.stream(data.getSeparators()).forEach(Separator::resetSeparator);
  }

  public void writeTablesToNetwork() {
    networkWriter.writeBackToCPTs();
  }

  public double getJointProbability() {
    return data.getJointProbability();
  }

  public void sumTransfer(Clique clique) {
    distributeEvidence(clique, new HashSet<>());
  }

  public double getJointProbOfMeasured(Collection<NodeState> newEvidence) {
    double cliqueSums = multiplyTableSums(data.getCliques(), Clique::getTable, newEvidence);
    double separatorSums =
        multiplyTableSums(data.getSeparators(), Separator::getTable, newEvidence);
    return separatorSums == 0.0 ? 0.0 : cliqueSums / separatorSums;
  }

  private void applyObservations(Map<Node, NodeState> observed) {
    Set<Node> nodesRemaining = new HashSet<>(observed.keySet());
    while (!nodesRemaining.isEmpty()) {
      ObservationOverlap bestOverlap = findBestOverlap(nodesRemaining, observed);
      nodesRemaining.removeAll(bestOverlap.nodeOverlap);
      Clique bestClique = bestOverlap.clique;
      bestClique.getHandler().setObserved(bestOverlap.evidenceStates);
      passMessages(bestClique);
    }
  }

  private ObservationOverlap findBestOverlap(
      Set<Node> nodesRemaining, Map<Node, NodeState> observed) {
    return Arrays.stream(data.getCliques())
        .map(c -> buildObservationOverlap(c, nodesRemaining, observed))
        .max(Comparator.comparingInt(c -> c.nodeOverlap.size()))
        .orElseThrow();
  }

  private ObservationOverlap buildObservationOverlap(
      Clique clique, Set<Node> nodesRemaining, Map<Node, NodeState> observed) {
    Set<Node> overlap = NodeUtils.getOverlap(clique.getNodes(), nodesRemaining);
    Set<NodeState> states = overlap.stream().map(observed::get).collect(Collectors.toSet());
    return new ObservationOverlap(clique, overlap, states);
  }

  private <T> double multiplyTableSums(
      T[] array, Function<T, JunctionTreeTable> tableFunction, Collection<NodeState> newEvidence) {
    return Arrays.stream(array)
        .map(tableFunction)
        .map(JunctionTreeTable::getHelper)
        .mapToDouble(helper -> helper.sumProbabilities(newEvidence))
        .reduce(1.0, (x, y) -> x * y);
  }

  private void resetObservations() {
    Arrays.stream(data.getCliques())
        .map(Clique::getHandler)
        .forEach(JunctionTreeTableHelper::resetObservations);
    Arrays.stream(data.getSeparators()).forEach(Separator::resetSeparator);
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

  private record ObservationOverlap(
      Clique clique, Set<Node> nodeOverlap, Set<NodeState> evidenceStates) {}
}
