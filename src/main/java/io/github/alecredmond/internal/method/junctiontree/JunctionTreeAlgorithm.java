package io.github.alecredmond.internal.method.junctiontree;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.inference.InferenceAlgorithm;
import io.github.alecredmond.internal.application.solver.SolverConfigs;
import io.github.alecredmond.internal.application.junctiontree.Clique;
import io.github.alecredmond.internal.application.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.junctiontree.Separator;
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
  private final MessagePasser messagePasser;

  public JunctionTreeAlgorithm(JunctionTreeData data) {
    this.data = data;
    this.networkWriter = new JTANetworkWriter(data);
    this.messagePasser = new MessagePasser(data);
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
    messagePasser.collectMessages(clique);
    messagePasser.distributeMessages(clique);
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
        .map(JunctionTreeTable::getQueryTool)
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
    messagePasser.distributeMessages(clique);
  }

  private record ObservationOverlap(
      Clique clique, Set<Node> nodeOverlap, Set<NodeState> evidenceStates) {}
}
