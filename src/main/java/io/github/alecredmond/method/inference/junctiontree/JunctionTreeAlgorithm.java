package io.github.alecredmond.method.inference.junctiontree;

import io.github.alecredmond.application.constraints.ParameterConstraint;
import io.github.alecredmond.application.inference.junctiontree.Clique;
import io.github.alecredmond.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.application.inference.junctiontree.Separator;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.junctiontree.JunctionTreeTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTATableHandlerSeparator;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class JunctionTreeAlgorithm {
  private final JunctionTreeData data;

  public JunctionTreeAlgorithm(JunctionTreeData data) {
    this.data = data;
    JTANetworkWriter.initializeJunctionTreeFromNetwork(data);
    marginalizeTables();
  }

    public void marginalizeTables() {
    data.getCliqueSet().stream().map(Clique::getHandler).forEach(JTATableHandler::marginalize);
    data.setMarginalized(true);
  }

  public void writeObservations() {
    JTANetworkWriter.writeToObservations(data);
  }

  public void observeNetwork(Map<Node, NodeState> observed) {
    if (data.getNodes().isEmpty()) return;
    JTANetworkWriter.setSeparatorsToUnity(data);
    setEvidence(observed);
    Clique clique = data.getLeafCliques().stream().findAny().orElseThrow();
    distributeAndCollectMessages(clique, new HashSet<>());
    data.setMarginalized(false);
  }

  public void writeTablesToNetwork() {
    JTANetworkWriter.writeToNetwork(data);
  }

  /**
   * Sums the values from the smallest table in the JTA. If the JTA has been marginalized, this will
   * re-run inference.
   */
  public double getProbabilityOfEvidence() {
    boolean resetMarginalisation = data.isMarginalized();
    if (resetMarginalisation) observeNetwork(data.getObserved());
    JunctionTreeTable smallestTable = data.getJunctionTreeTables().getFirst();
    double[] probabilityArray = smallestTable.getCorrectProbabilities();
    double jointProb = Arrays.stream(probabilityArray).sum();
    if (resetMarginalisation) marginalizeTables();
    return jointProb;
  }

  public boolean isMarginalized() {
    return data.isMarginalized();
  }

  double adjustAndReturnError(ParameterConstraint constraint) {
    Clique clique = data.getConstraintCliqueMap().get(constraint);
    distributeAndCollectMessages(clique, new HashSet<>());

    double error = data.getConstraintHandlers().get(constraint).adjustAndReturnError();

    if (error != 0) {
      distributeAndCollectMessages(clique, new HashSet<>());
    }

    return error;
  }

  private void setEvidence(Map<Node, NodeState> evidence) {
    data.setObserved(evidence);
    for (Clique clique : data.getCliqueSet()) {
      Set<NodeState> evidenceInTable =
          clique.getNodes().stream()
              .filter(evidence::containsKey)
              .map(evidence::get)
              .collect(Collectors.toSet());

      clique.getHandler().setObserved(evidenceInTable, !evidenceInTable.isEmpty());
    }
  }

  private void distributeAndCollectMessages(Clique currentClique, Set<Clique> cliqueChain) {
    cliqueChain.add(currentClique);

    getNextSeparators(currentClique, cliqueChain)
        .forEach(
            (nextClique, separator) -> {
              JTATableHandlerSeparator sth = separator.getHandler();
              sth.passMessageFrom(currentClique);
              distributeAndCollectMessages(nextClique, cliqueChain);
              sth.passMessageFrom(nextClique);
            });

    cliqueChain.remove(currentClique);
  }

  private Map<Clique, Separator> getNextSeparators(Clique currentClique, Set<Clique> cliqueChain) {
    return currentClique.getSeparatorMap().entrySet().stream()
        .filter(entry -> !cliqueChain.contains(entry.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
