package io.github.alecredmond.method.sampler.jtasampler;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.application.sampler.Clique;
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
              initialiseIndexPointers(clique.getTable(), networkTables);
              mapProbabilitiesAndIndexes(clique, networkTables);
            });
    setSeparatorsToUnity();
  }

  private void initialiseIndexPointers(
      JunctionTreeTable cliqueTable, Set<ProbabilityTable> networkTables) {
    networkTables.forEach(
        table ->
            cliqueTable
                .getIndexPointerMap()
                .put(table, new Integer[cliqueTable.getProbabilities().length]));
  }

  /**
   * When a clique table [e.g P(A,B,C)] is initialized and assigned network tables [e.g
   * P(A)*P(B)*P(C|A,B)], this function fills the joint probabilities on the clique table
   * accordingly and assigns a pointer from the clique table probability index to the associated
   * indexes on each included network table
   */
  private void mapProbabilitiesAndIndexes(Clique clique, Set<ProbabilityTable> networkTables) {
    JunctionTreeTable cliqueTable = clique.getTable();

    for (Set<NodeState> request : cliqueTable.getKeySet()) {
      int cliqueIndex = cliqueTable.getIndex(request);

      double jointProb = 1.0;

      for (ProbabilityTable table : networkTables) {
        Set<NodeState> validRequest = TableUtils.removeRedundantStates(request, table);
        int networkIndexPointer = table.getIndex(validRequest);
        cliqueTable.getIndexPointerMap().get(table)[cliqueIndex] = networkIndexPointer;
        double tableProb = table.getProbabilities()[networkIndexPointer];
        jointProb = jointProb * tableProb;
      }

      cliqueTable.getProbabilities()[cliqueIndex] = jointProb;
    }
  }

  /** Separators are initialized with all of their table values set to 1.0 */
  protected void setSeparatorsToUnity() {
    data.getSeparators().stream()
        .map(Separator::getTable)
        .forEach(table -> Arrays.fill(table.getProbabilities(), 1.0));
  }

  protected void writeToObservations(Map<Node, NodeState> observedStates) {
    data.getBayesianNetworkData().setObservedStatesMap(observedStates);
    for (Node node : data.getNodes()) {
      JunctionTableHandler handler = getHandlerForSmallestRelevantClique(node);
      MarginalTable observedTable = data.getObservationMap().get(node);
      writeToMarginalTable(observedTable, handler);
      updateTableName(node, observedTable, observedStates.values());
    }
  }

  private JunctionTableHandler getHandlerForSmallestRelevantClique(Node node) {
    return data.getCliqueSet().stream()
        .filter(clique -> clique.getNodes().contains(node))
        .min(Comparator.comparingInt(table -> table.getNodes().size()))
        .orElseThrow()
        .getHandler();
  }

  private void writeToMarginalTable(MarginalTable marginalTable, JunctionTableHandler handler) {
    marginalTable
        .getKeySet()
        .forEach(key -> marginalTable.setProbability(key, handler.sumFromTable(key)));
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
