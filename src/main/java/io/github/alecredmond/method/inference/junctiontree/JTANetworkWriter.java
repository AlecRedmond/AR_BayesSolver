package io.github.alecredmond.method.inference.junctiontree;

import io.github.alecredmond.application.inference.junctiontree.Clique;
import io.github.alecredmond.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.application.inference.junctiontree.Separator;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.junctiontree.JunctionTreeTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.method.probabilitytables.TableUtils;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class JTANetworkWriter {

  private JTANetworkWriter() {}

  static void initializeJunctionTreeFromNetwork(JunctionTreeData data) {
    data.getAssociatedTables()
        .forEach(
            (clique, networkTables) -> {
              initialiseIndexPointers(clique.getTable(), networkTables);
              mapProbabilitiesAndIndexes(clique, networkTables);
            });
    setSeparatorsToUnity(data);
  }

  private static void initialiseIndexPointers(
      JunctionTreeTable cliqueTable, Set<ProbabilityTable> networkTables) {
    networkTables.forEach(
        table ->
            cliqueTable
                .getIndexPointerMap()
                .put(table, new Integer[cliqueTable.getProbabilities().length]));
  }

  /**
   * When a clique table [e.g P(A,B,C)] is assigned network tables [e.g P(A)*P(B)*P(C|A,B)], this
   * fills the joint probabilities and maps each clique's index to the relevant state's index on the
   * network tables. This is used for summing up state probabilities during write-back.
   */
  private static void mapProbabilitiesAndIndexes(
      Clique clique, Set<ProbabilityTable> networkTables) {
    JunctionTreeTable cliqueTable = clique.getTable();

    for (Set<NodeState> request : cliqueTable.getKeySet()) {
      int cliqueIndex = cliqueTable.getIndex(request);

      double jointProb = 1.0;

      for (ProbabilityTable table : networkTables) {
        Set<NodeState> validRequest = TableUtils.collectStatesPresentInTable(request, table);
        int networkIndexPointer = table.getIndex(validRequest);
        cliqueTable.getIndexPointerMap().get(table)[cliqueIndex] = networkIndexPointer;
        double tableProb = table.getProbabilities()[networkIndexPointer];
        jointProb = jointProb * tableProb;
      }

      cliqueTable.getProbabilities()[cliqueIndex] = jointProb;
    }
  }

  
  static void setSeparatorsToUnity(JunctionTreeData data) {
    data.getSeparators().stream()
        .map(Separator::getTable)
        .forEach(table -> Arrays.fill(table.getProbabilities(), 1.0));
  }

  static void writeToObservations(JunctionTreeData data) {
    data.getBayesianNetworkData().setObserved(data.getObserved());
    for (Node node : data.getNodes()) {
      JTATableHandler handler = getHandlerForSmallestRelevantClique(data, node);
      MarginalTable observedTable = data.getObservationMap().get(node);
      writeToMarginalTable(observedTable, handler);
      updateTableName(node, observedTable, data.getObserved().values());
    }
  }

  private static JTATableHandler getHandlerForSmallestRelevantClique(
      JunctionTreeData data, Node node) {
    return data.getCliqueSet().stream()
        .filter(clique -> clique.getNodes().contains(node))
        .min(Comparator.comparingInt(table -> table.getNodes().size()))
        .orElseThrow()
        .getHandler();
  }

  private static void writeToMarginalTable(MarginalTable marginalTable, JTATableHandler handler) {
    marginalTable
        .getKeySet()
        .forEach(key -> marginalTable.setProbability(key, handler.sumFromTable(key)));
  }

  private static void updateTableName(
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

  static void writeToNetwork(JunctionTreeData data) {
    log.info("WRITING TO NETWORK");
    data.getAssociatedTables()
        .forEach(
            ((clique, netTables) ->
                netTables.forEach(nt -> writeNetworkTable(clique.getTable(), nt))));
    log.info("NETWORK TABLES WRITTEN");
  }

  static void writeNetworkTable(JunctionTreeTable cliqueTable, ProbabilityTable networkTable) {
    double[] networkProbs = networkTable.getProbabilities();
    Arrays.fill(networkProbs, 0.0);
    double[] cliqueProbs = cliqueTable.getProbabilities();
    Integer[] indexPointers = cliqueTable.getIndexPointerMap().get(networkTable);

    for (int cliqueIndex = 0; cliqueIndex < cliqueProbs.length; cliqueIndex++) {
      int networkProbIndex = indexPointers[cliqueIndex];
      networkProbs[networkProbIndex] = networkProbs[networkProbIndex] + cliqueProbs[cliqueIndex];
    }

    if (networkTable instanceof ConditionalTable) TableUtils.marginalizeTable(networkTable);
  }
}
