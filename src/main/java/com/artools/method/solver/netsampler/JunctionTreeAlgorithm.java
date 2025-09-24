package com.artools.method.solver.netsampler;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.junctiontree.Clique;
import com.artools.application.junctiontree.JunctionTreeData;
import com.artools.application.junctiontree.Separator;
import com.artools.application.network.BayesNetData;
import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.JunctionTreeTable;
import com.artools.method.probabilitytables.TableUtils;
import java.util.*;
import java.util.stream.Collectors;

public class JunctionTreeAlgorithm implements NetworkSampler {
  private final JunctionTreeData data;
  private final IterativeProportionalFitter fitter;
  private final NetworkJunctionConverter converter;

  public JunctionTreeAlgorithm(BayesNetData networkData) {
    this.data = JunctionTreeDataBuilder.build(networkData);
    this.fitter = new IterativeProportionalFitter();
    this.converter = new NetworkJunctionConverter(data);
    this.converter.initializeJunctionTreeFromNetwork();
  }

  @Override
  public double adjustAndReturnError(ParameterConstraint constraint) {
    Clique clique = data.getCliqueForConstraint().get(constraint);
    distributeAndCollectMessages(clique, new HashSet<>(), null);

    double error = fitter.fitData(constraint, clique.getTable()).getError();

    if (error != 0) {
      distributeAndCollectMessages(clique, new HashSet<>(), null);
      marginalizeTables();
    }
    return error;
  }

  @Override
  public void sampleNetwork(Map<Node, NodeState> evidence) {
    converter.setSeparatorsToUnity();
    setEvidence(evidence);
    Clique startingClique = data.getLeafCliques().stream().findAny().orElseThrow();
    distributeAndCollectMessages(startingClique, new HashSet<>(), null);
    marginalizeTables();
    converter.writeToObservations();
  }

  public void sampleNetwork() {
    sampleNetwork(new HashMap<>());
  }

  public void writeTablesToNetwork() {
    converter.writeToNetwork();
  }

  private void marginalizeTables() {
    data.getJunctionTreeTables().forEach(TableUtils::marginalizeTable);
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

  private void distributeAndCollectMessages(
      Clique currentClique, Set<Clique> cliqueChain, Separator lastSeparator) {
    cliqueChain.add(currentClique);

    boolean hasLastSeparator = Optional.ofNullable(lastSeparator).isPresent();
    getMessageFromLastSeparator(currentClique, lastSeparator, hasLastSeparator);

    getNextSeparators(currentClique, cliqueChain)
        .forEach(
            (nextClique, separator) -> {
              passMessage(currentClique.getTable(), separator.getTable(), true);
              distributeAndCollectMessages(nextClique, cliqueChain, separator);
              passMessage(currentClique.getTable(), separator.getTable(), false);
            });

    passMessageToLastSeparator(currentClique, lastSeparator, hasLastSeparator);

    cliqueChain.remove(currentClique);
  }

  private void passMessageToLastSeparator(
      Clique currentClique, Separator lastSeparator, boolean hasLastSeparator) {
    if (hasLastSeparator) passMessage(currentClique.getTable(), lastSeparator.getTable(), true);
  }

  private void getMessageFromLastSeparator(
      Clique currentClique, Separator lastSeparator, boolean hasLastSeparator) {
    if (hasLastSeparator) passMessage(currentClique.getTable(), lastSeparator.getTable(), false);
  }

  private Map<Clique, Separator> getNextSeparators(Clique currentClique, Set<Clique> cliqueChain) {
    return currentClique.getSeparatorMap().entrySet().stream()
        .filter(entry -> !cliqueChain.contains(entry.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private void passMessage(
      JunctionTreeTable cliqueTable, JunctionTreeTable separatorTable, boolean toSeparator) {
    separatorTable
        .getKeySet()
        .forEach(
            separatorTableKey -> {
              Set<Set<NodeState>> cliqueTableKey =
                  cliqueTable.getKeySet().stream()
                      .filter(key -> key.containsAll(separatorTableKey))
                      .collect(Collectors.toSet());

              double cliqueSum =
                  cliqueTableKey.stream().mapToDouble(cliqueTable::getCorrectProb).sum();

              if (toSeparator) {
                separatorTable.setCorrectProb(separatorTableKey, cliqueSum);
                return;
              }

              double separatorSum = separatorTable.getCorrectProb(separatorTableKey);
              double ratio = cliqueSum == 0 ? 0.0 : separatorSum / cliqueSum;
              cliqueTableKey.forEach(cr -> cliqueTable.setCorrectByRatio(cr, ratio));
            });
  }
}
