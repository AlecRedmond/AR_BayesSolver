package io.github.alecredmond.method.sampler.jtasampler;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.application.sampler.JunctionTreeData;
import io.github.alecredmond.application.sampler.Separator;
import io.github.alecredmond.method.probabilitytables.TableUtils;
import io.github.alecredmond.method.sampler.jtasampler.jtahandlers.JunctionTableHandler;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkJunctionConverter {
  private final JunctionTreeData data;

  protected NetworkJunctionConverter(JunctionTreeData data) {
    this.data = data;
  }

  protected void initializeJunctionTreeFromNetwork() {
    data.getAssociatedTables()
        .forEach(
            (clique, networkTables) -> {
              initializeEquivalentIndexes(clique.getTable(), networkTables);
              clique
                  .getTable()
                  .getKeySet()
                  .forEach(
                      request ->
                          setProbabilityAndSubTableIndexes(
                              clique.getTable(), networkTables, request));
            });
    setSeparatorsToUnity();
  }

  private void initializeEquivalentIndexes(
      JunctionTreeTable cliqueTable, Set<ProbabilityTable> networkTables) {
    networkTables.forEach(
        table ->
            cliqueTable
                .getEquivalentIndexMap()
                .put(table, new Integer[cliqueTable.getProbabilities().length]));
  }

  private void setProbabilityAndSubTableIndexes(
      JunctionTreeTable cliqueTable, Set<ProbabilityTable> tableSet, Set<NodeState> request) {

    int cliqueTableKeyIndex = cliqueTable.getIndex(request);

    double jointProb = 1.0;

    for (ProbabilityTable table : tableSet) {
      Set<NodeState> validRequest = TableUtils.removeRedundantStates(request, table);
      int networkTableKeyIndex = table.getIndex(validRequest);
      cliqueTable.getEquivalentIndexMap().get(table)[cliqueTableKeyIndex] = networkTableKeyIndex;
      double tableProb = table.getProbabilities()[networkTableKeyIndex];
      jointProb = jointProb * tableProb;
    }

    cliqueTable.getProbabilities()[cliqueTableKeyIndex] = jointProb;
  }

  protected void setSeparatorsToUnity() {
    data.getSeparators().stream()
        .map(Separator::getTable)
        .forEach(table -> Arrays.fill(table.getProbabilities(), 1.0));
  }

  protected void writeToObservations(Map<Node, NodeState> observedStates) {
    data.getBayesianNetworkData().setObservedStatesMap(observedStates);
    for (Node node : data.getNodes()) {
      JunctionTableHandler indexer = getBestTableIndexer(node);
      MarginalTable observedTable = data.getObservationMap().get(node);
      writeToMarginalTable(observedTable, indexer);
      updateTableName(node, observedTable, observedStates.values());
    }
  }

  private JunctionTableHandler getBestTableIndexer(Node node) {
    return data.getCliqueSet().stream()
        .filter(clique -> clique.getNodes().contains(node))
        .min(Comparator.comparingInt(table -> table.getNodes().size()))
        .orElseThrow()
        .getHandler();
  }

  private void writeToMarginalTable(MarginalTable marginalTable, JunctionTableHandler indexer) {
    marginalTable
        .getKeySet()
        .forEach(key -> marginalTable.setProbability(key, indexer.sumFromTable(key)));
  }

  private void updateTableName(
      Node node, MarginalTable observedTable, Collection<NodeState> states) {
    StringBuilder sb = new StringBuilder("P(").append(node.getNodeID().toString());
    if (!states.isEmpty()) {
      sb.append("|");
      states.forEach(observed -> sb.append(observed.getStateID().toString()).append(","));
      sb.deleteCharAt(sb.length() - 1);
    }
    sb.append(")");
    observedTable.setTableID(sb.toString());
  }

  protected void writeToNetwork() {
    log.info("WRITING TO NETWORK");
    data.getAssociatedTables()
        .forEach(
            ((clique, netTables) ->
                netTables.forEach(nt -> writeNetworkTable(clique.getTable(), nt))));
    log.info("NETWORK TABLES WRITTEN");
  }

  protected void writeNetworkTable(JunctionTreeTable cliqueTable, ProbabilityTable networkTable) {
    double[] netProbs = networkTable.getProbabilities();
    Arrays.fill(netProbs, 0.0);
    double[] cliqueProbs = cliqueTable.getProbabilities();
    Integer[] equivalentIndexes = cliqueTable.getEquivalentIndexMap().get(networkTable);

    for (int cliqueIndex = 0; cliqueIndex < cliqueProbs.length; cliqueIndex++) {
      int netIndex = equivalentIndexes[cliqueIndex];
      netProbs[netIndex] = netProbs[netIndex] + cliqueProbs[cliqueIndex];
    }

    if (networkTable instanceof ConditionalTable) TableUtils.marginalizeTable(networkTable);
  }
}
