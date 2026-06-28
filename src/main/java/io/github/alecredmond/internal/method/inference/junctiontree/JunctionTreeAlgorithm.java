package io.github.alecredmond.internal.method.inference.junctiontree;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.inference.InferenceAlgorithm;
import io.github.alecredmond.internal.application.inference.SolverConfigs;
import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.inference.junctiontree.Separator;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.node.NodeUtils;
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
      BayesianNetworkData bnd, InferenceAlgorithm inferenceAlgorithm) {
    return new JunctionTreeAlgorithm(
        new JTADataBuilder().buildNewInferenceConfiguration(bnd, inferenceAlgorithm));
  }

  public void rebuildJTA(BayesianNetworkData bnd, InferenceAlgorithm inferenceAlgorithm) {
    new JTADataBuilder().buildInferenceConfiguration(data, bnd, inferenceAlgorithm);
    networkWriter.initializeJunctionTreeFromNetwork();
  }

  public void observeNetwork(Map<Node, NodeState> observed) {
    resetObservations();
    if (observed.isEmpty()) passMessages(data.getCliques()[0]);
    else applyObservations(observed);
    data.setObservedEvidence(observed);
    data.setJointProbability(getJointProbOfMeasured(new HashSet<>()));
    networkWriter.writeObservations();
  }

  private void resetObservations() {
    Arrays.stream(data.getCliques()).forEach(Clique::resetObservations);
    Arrays.stream(data.getSeparators()).forEach(Separator::resetSeparator);
  }

  private void passMessages(Clique clique) {
    collectEvidence(clique);
    distributeEvidence(clique);
  }

  private void applyObservations(Map<Node, NodeState> observed) {
    Set<Node> nodesRemaining = new HashSet<>(observed.keySet());
    while (!nodesRemaining.isEmpty()) {
      ObservationOverlap overlap = findLargestOverlap(nodesRemaining, observed);
      nodesRemaining.removeAll(overlap.nodeOverlap);
      Clique clique = overlap.clique;
      clique.setObserved(overlap.evidenceStates);
      passMessages(clique);
    }
  }

  public double getJointProbOfMeasured(Collection<NodeState> newEvidence) {
    double separatorSums = productOfSums(data.getSeparators(), Separator::getTable, newEvidence);
    if (separatorSums == 0.0) return 0.0;
    double cliqueSums = productOfSums(data.getCliques(), Clique::getTable, newEvidence);
    return cliqueSums / separatorSums;
  }

  private void collectEvidence(Clique startClique) {
    Queue<Clique> queue = new ArrayDeque<>();
    Set<Clique> visited = new HashSet<>();
    List<Runnable> collectionRuns = new ArrayList<>();
    queue.add(startClique);
    while (!queue.isEmpty()) {
      Clique clique = queue.poll();
      visited.add(clique);
      clique
          .getSeparatorMap()
          .forEach(
              (nextClique, separator) -> {
                if (visited.contains(nextClique)) return;
                collectionRuns.add(() -> separator.passMessageFrom(nextClique));
                queue.add(nextClique);
              });
    }
    collectionRuns.reversed().forEach(Runnable::run);
  }

  private void distributeEvidence(Clique startClique) {
    Queue<Clique> queue = new ArrayDeque<>();
    Set<Clique> visited = new HashSet<>();
    queue.add(startClique);
    while (!queue.isEmpty()) {
      Clique clique = queue.poll();
      visited.add(clique);
      clique
          .getSeparatorMap()
          .forEach(
              (nextClique, separator) -> {
                if (visited.contains(nextClique)) return;
                separator.passMessageFrom(clique);
                queue.add(nextClique);
              });
    }
  }

  private ObservationOverlap findLargestOverlap(
      Set<Node> nodesRemaining, Map<Node, NodeState> observed) {
    return Arrays.stream(data.getCliques())
        .map(c -> buildObservationOverlap(c, nodesRemaining, observed))
        .filter(Objects::nonNull)
        .max(Comparator.comparingInt(c -> c.nodeOverlap.size()))
        .orElseThrow();
  }

  private <T> double productOfSums(
      T[] array, Function<T, JunctionTreeTable> tableFunction, Collection<NodeState> newEvidence) {
    return Arrays.stream(array)
        .map(tableFunction)
        .map(JunctionTreeTable::getHelper)
        .mapToDouble(helper -> helper.sumProbabilities(newEvidence))
        .reduce(1.0, (x, y) -> x * y);
  }

  private ObservationOverlap buildObservationOverlap(
      Clique clique, Set<Node> nodesRemaining, Map<Node, NodeState> observed) {
    Set<Node> overlap = NodeUtils.getOverlap(clique.getNodes(), nodesRemaining);
    if (overlap.isEmpty()) return null;
    Set<NodeState> states = overlap.stream().map(observed::get).collect(Collectors.toSet());
    return new ObservationOverlap(clique, overlap, states);
  }

  public void normalizeTables() {
    Arrays.stream(data.getCliques()).forEach(Clique::normalizeTable);
    Arrays.stream(data.getSeparators()).forEach(Separator::resetSeparator);
  }

  public void writeTablesToNetwork() {
    networkWriter.writeBackToCPTs();
  }

  public double getJointProbability() {
    return data.getJointProbability();
  }

  public void sumTransfer(Clique clique) {
    distributeEvidence(clique);
  }

  private record ObservationOverlap(
      Clique clique, Set<Node> nodeOverlap, Set<NodeState> evidenceStates) {}
}
