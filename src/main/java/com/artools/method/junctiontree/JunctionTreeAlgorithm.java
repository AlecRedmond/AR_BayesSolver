package com.artools.method.junctiontree;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.junctiontree.Clique;
import com.artools.application.junctiontree.JunctionTreeData;
import com.artools.application.junctiontree.Separator;
import com.artools.application.network.BayesNetData;
import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.method.indexer.SeparatorTableIndexer;
import com.artools.method.indexer.TableIndexer;
import com.artools.method.probabilitytables.TableUtils;
import com.artools.method.solver.NetworkSampler;
import java.util.*;
import java.util.stream.Collectors;

public class JunctionTreeAlgorithm implements NetworkSampler {
  private final JunctionTreeData data;
  private final NetworkJunctionConverter converter;

  public JunctionTreeAlgorithm(BayesNetData networkData) {
    System.out.println("JTA INIT");
    this.data = JunctionTreeDataBuilder.build(networkData);
    System.out.println("JTA DATA BUILT");
    this.converter = new NetworkJunctionConverter(data);
    this.converter.initializeJunctionTreeFromNetwork();
    marginalizeTables();
    System.out.println("JTA MARGINALIZED");
  }

  private void marginalizeTables() {
    data.getCliqueSet().stream().map(Clique::getIndexer).forEach(TableIndexer::marginalize);
  }

  @Override
  public double adjustAndReturnError(ParameterConstraint constraint) {
    Clique clique = data.getCliqueForConstraint().get(constraint);
    distributeAndCollectMessages(clique, new HashSet<>());

    double error = data.getConstraintIndexerMap().get(constraint).adjustAndReturnError();

    if (error != 0) {
      distributeAndCollectMessages(clique, new HashSet<>());
    }

    return error;
  }

  @Override
  public void sampleNetwork(Map<Node, NodeState> observedStates) {
    converter.setSeparatorsToUnity();
    setEvidence(observedStates);
    Clique clique = data.getLeafCliques().stream().findAny().orElseThrow();
    distributeAndCollectMessages(clique, new HashSet<>());
    converter.writeToObservations();
  }

  public void sampleNetwork() {
    sampleNetwork(new HashMap<>());
  }

  public void writeTablesToNetwork() {
    converter.writeToNetwork();
  }

  private void setEvidence(Map<Node, NodeState> evidence) {
    data.getJunctionTreeTables()
        .forEach(
            table -> {
              Set<NodeState> evidenceInTable =
                  table.getNodes().stream()
                      .filter(evidence::containsKey)
                      .map(evidence::get)
                      .collect(Collectors.toSet());

              table.setObserved(evidenceInTable, !evidence.isEmpty());
              TableUtils.recalculateObservedProbabilityMap(table);
            });
  }

  private void distributeAndCollectMessages(Clique currentClique, Set<Clique> cliqueChain) {
    cliqueChain.add(currentClique);

    getNextSeparators(currentClique, cliqueChain)
        .forEach(
            (nextClique, separator) -> {
              SeparatorTableIndexer si = separator.getTableIndexer();
              si.passMessage(currentClique);
              distributeAndCollectMessages(nextClique, cliqueChain);
              si.passMessage(nextClique);
            });

    cliqueChain.remove(currentClique);
  }

  private Map<Clique, Separator> getNextSeparators(Clique currentClique, Set<Clique> cliqueChain) {
    return currentClique.getSeparatorMap().entrySet().stream()
        .filter(entry -> !cliqueChain.contains(entry.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
