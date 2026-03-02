package io.github.alecredmond.method.inference.junctiontree;

import io.github.alecredmond.application.inference.junctiontree.Clique;
import io.github.alecredmond.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.export.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.internal.JunctionTreeTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.readwrite.JTATransferWriter;
import java.util.*;
import java.util.stream.Collectors;
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
    distributeAndCollectMessages(bestClique, new HashSet<>());
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
    JunctionTreeTable smallestTable = data.getJunctionTreeTables().getFirst();
    double[] probabilityArray = smallestTable.getVector().getProbabilities();
    return Arrays.stream(probabilityArray).sum();
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

    for (Clique clique : data.getCliques()) {
      Set<NodeState> evidenceInTable =
          clique.getNodes().stream()
              .filter(evidence::containsKey)
              .map(evidence::get)
              .collect(Collectors.toSet());

      clique.getHandler().setObserved(evidenceInTable, !evidenceInTable.isEmpty());
    }
  }

  void distributeAndCollectMessages(Clique clique) {
    distributeAndCollectMessages(clique, new HashSet<>());
  }

  private void distributeAndCollectMessages(Clique currentClique, Set<Clique> cliqueChain) {
    cliqueChain.add(currentClique);

    getNextSeparators(currentClique, cliqueChain)
        .forEach(
            (nextClique, separator) -> {
              JTATransferWriter backSeparator = nextClique.getSeparator(currentClique);
              separator.run();
              distributeAndCollectMessages(nextClique, cliqueChain);
              backSeparator.run();
            });

    cliqueChain.remove(currentClique);
  }

  private Map<Clique, JTATransferWriter> getNextSeparators(
      Clique currentClique, Set<Clique> cliqueChain) {
    return currentClique.getSeparatorMap().entrySet().stream()
        .filter(entry -> !cliqueChain.contains(entry.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
