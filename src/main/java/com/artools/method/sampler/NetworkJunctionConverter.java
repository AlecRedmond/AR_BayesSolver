package com.artools.method.sampler;

import com.artools.application.sampler.JunctionTreeData;
import com.artools.application.sampler.Separator;
import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.ConditionalTable;
import com.artools.application.probabilitytables.JunctionTreeTable;
import com.artools.application.probabilitytables.MarginalTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import com.artools.method.indexer.TableIndexer;
import com.artools.method.probabilitytables.TableUtils;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkJunctionConverter {
  private final JunctionTreeData data;

  public NetworkJunctionConverter(JunctionTreeData data) {
    this.data = data;
  }

  public void initializeJunctionTreeFromNetwork() {
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

    int cliqueTableKeyIndex = cliqueTable.getIndex(request, false);

    double jointProb = 1.0;

    for (ProbabilityTable table : tableSet) {
      int networkTableKeyIndex = table.getIndex(request, true);
      cliqueTable.getEquivalentIndexMap().get(table)[cliqueTableKeyIndex] = networkTableKeyIndex;
      double tableProb = table.getProbabilities()[networkTableKeyIndex];
      jointProb = jointProb * tableProb;
    }

    cliqueTable.getProbabilities()[cliqueTableKeyIndex] = jointProb;
  }

  public void setSeparatorsToUnity() {
    data.getSeparators().stream()
        .map(Separator::getTable)
        .forEach(table -> Arrays.fill(table.getProbabilities(), 1.0));
  }

  public void writeToObservations() {
    for (Node node : data.getNodes()) {
      TableIndexer indexer = getBestTableIndexer(node);
      MarginalTable observedTable = data.getObservationMap().get(node);
      writeToMarginalTable(observedTable, indexer);
    }
  }

  private TableIndexer getBestTableIndexer(Node node) {
    return data.getCliqueSet().stream()
        .filter(clique -> clique.getNodes().contains(node))
        .min(Comparator.comparingInt(table -> table.getNodes().size()))
        .orElseThrow()
        .getIndexer();
  }

  private void writeToMarginalTable(MarginalTable marginalTable, TableIndexer indexer) {
    marginalTable
        .getKeySet()
        .forEach(key -> marginalTable.setProbability(key, indexer.sumFromTable(key)));
  }

  public void writeToNetwork() {
    log.info("WRITING TO NETWORK");
    data.getAssociatedTables()
        .forEach(
            ((clique, netTables) ->
                netTables.forEach(nt -> writeNetworkTable(clique.getTable(), nt))));
    log.info("NETWORK TABLES WRITTEN");
  }

  private void writeNetworkTable(JunctionTreeTable cliqueTable, ProbabilityTable networkTable) {
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
