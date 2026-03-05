package io.github.alecredmond.method.inference.junctiontree;

import io.github.alecredmond.application.inference.junctiontree.Clique;
import io.github.alecredmond.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.application.inference.junctiontree.Separator;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.export.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.internal.JunctionTreeTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.method.probabilitytables.TableUtils;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public class JunctionTreeAlgorithm {
  private final JunctionTreeData data;

  public JunctionTreeAlgorithm(JunctionTreeData data) {
    this.data = data;
    JTANetworkWriter.initializeJunctionTreeFromNetwork(data);
  }

  public void writeObservations() {
    JTANetworkWriter.writeObservations(data);
  }

  public void observeNetwork(Map<Node, NodeState> observed) {
    if (data.getNodes().isEmpty()) return;
    Clique bestClique = cliqueWithLargestOverlap(observed);
    setEvidence(observed);
    collectAndDistributeMessages(bestClique);
  }

  public void marginalizeTables() {
    Arrays.stream(data.getCliques())
        .map(Clique::getTable)
        .forEach(ProbabilityTable::marginalizeTable);
  }

  public void writeTablesToNetwork() {
    JTANetworkWriter.writeToNetwork(data);
  }

  public double getProbabilityOfEvidence() {
    double cliqueProb = multiplyTableSums(data.getCliques(), Clique::getTable, TableUtils::sumAll);
    double separatorProb =
        multiplyTableSums(data.getSeparators(), Separator::getTable, TableUtils::sumAll);
    return separatorProb == 0.0 ? 0.0 : cliqueProb / separatorProb;
  }

  private <T> double multiplyTableSums(
      T[] array,
      Function<T, JunctionTreeTable> tableFunction,
      ToDoubleFunction<JunctionTreeTable> toDoubleFunction) {
    return Arrays.stream(array)
        .map(tableFunction)
        .mapToDouble(toDoubleFunction)
        .reduce(1.0, (x, y) -> x * y);
  }

  public double getProbabilityOfNewEvidence(Collection<NodeState> newEvidence) {
    ToDoubleFunction<JunctionTreeTable> toDoubleFunction =
        table -> TableUtils.sumProbabilities(newEvidence, table);
    double cliqueProb = multiplyTableSums(data.getCliques(), Clique::getTable, toDoubleFunction);
    double separatorProb =
        multiplyTableSums(data.getSeparators(), Separator::getTable, toDoubleFunction);
    return separatorProb == 0.0 ? 0.0 : cliqueProb / separatorProb;
  }

  private Clique cliqueWithLargestOverlap(Map<Node, NodeState> observed) {
    return Arrays.stream(data.getCliques())
        .max(
            Comparator.comparingInt(
                c -> {
                  Set<Node> temp = new HashSet<>(c.getNodes());
                  temp.retainAll(observed.keySet());
                  return temp.size();
                }))
        .orElseThrow();
  }

  private void setEvidence(Map<Node, NodeState> evidence) {
    data.setObserved(evidence);

    for (JTATableHandler handler : getAllHandlers()) {
      Set<NodeState> evidenceInTable =
          handler.getTable().getNodes().stream()
              .filter(evidence::containsKey)
              .map(evidence::get)
              .collect(Collectors.toSet());

      handler.setObserved(evidenceInTable, !evidenceInTable.isEmpty());
    }
  }

  private List<JTATableHandler> getAllHandlers() {
    return Stream.concat(
            Arrays.stream(data.getCliques()).map(Clique::getHandler),
            Arrays.stream(data.getSeparators()).map(Separator::getHandler))
        .toList();
  }

  void collectAndDistributeMessages(Clique clique) {
    Set<Clique> visited = new HashSet<>();
    collectEvidence(clique, visited);
    visited.clear();
    distributeEvidence(clique, visited);
  }

  void collectEvidence(Clique currentClique, Set<Clique> visited) {
    visited.add(currentClique);
    getNextSeparators(currentClique, visited)
        .forEach(
            (nextClique, separator) -> {
              collectEvidence(nextClique, visited);
              separator.run(nextClique);
            });
  }

  void distributeEvidence(Clique currentClique, Set<Clique> visited) {
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
