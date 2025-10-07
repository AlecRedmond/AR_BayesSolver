package com.artools.method.sampler.jtasampler;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.network.BayesianNetworkData;
import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.sampler.Clique;
import com.artools.application.sampler.JunctionTreeData;
import com.artools.application.sampler.Separator;
import com.artools.method.sampler.jtasampler.jtahandlers.JunctionTableHandler;
import com.artools.method.sampler.jtasampler.jtahandlers.SeparatorTableHandler;
import java.util.*;
import java.util.stream.Collectors;

public class JunctionTreeAlgorithm {
  private final JunctionTreeData data;
  private final NetworkJunctionConverter converter;

  public JunctionTreeAlgorithm(BayesianNetworkData networkData) {
    this.data = JunctionTreeDataBuilder.build(networkData);
    this.converter = new NetworkJunctionConverter(data);
    this.converter.initializeJunctionTreeFromNetwork();
    marginalizeTables();
  }

  private void marginalizeTables() {
    data.getCliqueSet().stream().map(Clique::getHandler).forEach(JunctionTableHandler::marginalize);
  }

    public double adjustAndReturnError(ParameterConstraint constraint) {
    Clique clique = data.getCliqueForConstraint().get(constraint);
    distributeAndCollectMessages(clique, new HashSet<>());

    double error = data.getConstraintHandlers().get(constraint).adjustAndReturnError();

    if (error != 0) {
      distributeAndCollectMessages(clique, new HashSet<>());
    }

    return error;
  }

  public void sampleNetwork(Map<Node,NodeState> observed) {
    converter.setSeparatorsToUnity();
    setEvidence(observed);
    Clique clique = data.getLeafCliques().stream().findAny().orElseThrow();
    distributeAndCollectMessages(clique, new HashSet<>());
    converter.writeToObservations(observed);
  }

  public void writeTablesToNetwork() {
    converter.writeToNetwork();
  }

  private void setEvidence(Map<Node, NodeState> evidence) {
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
              SeparatorTableHandler sth = separator.getHandler();
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
