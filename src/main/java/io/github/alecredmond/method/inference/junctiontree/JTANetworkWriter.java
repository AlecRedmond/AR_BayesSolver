package io.github.alecredmond.method.inference.junctiontree;

import io.github.alecredmond.application.inference.junctiontree.Clique;
import io.github.alecredmond.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.application.inference.junctiontree.Separator;
import io.github.alecredmond.application.network.BayesianNetworkData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.export.MarginalTable;
import io.github.alecredmond.application.probabilitytables.export.ProbabilityTable;
import io.github.alecredmond.method.probabilitytables.transfer.TransferIterator;
import java.util.*;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class JTANetworkWriter {

  private JTANetworkWriter() {}

  static void initializeJunctionTreeFromNetwork(JunctionTreeData jtd) {
    for (Clique clique : jtd.getCliques()) {
      setProbabilitiesToUnity(clique);
      clique.getWriteFromCPTs().forEach(TransferIterator::transfer);
      clique.getTable().marginalizeTable();
    }
    backupUnobservedData(jtd);
    marginaliseSeparators(jtd);
  }

    private static void setProbabilitiesToUnity(Clique clique) {
    Arrays.fill(clique.getTable().getVector().getProbabilities(), 1.0);
  }

  private static void backupUnobservedData(JunctionTreeData data) {
    Arrays.stream(data.getCliques())
        .map(Clique::getTable)
        .forEach(
            jtt -> {
              double[] solvedProbabilities = jtt.getVector().getProbabilities();
              double[] backup = jtt.getBackupVector().getProbabilities();
              System.arraycopy(solvedProbabilities, 0, backup, 0, backup.length);
            });
  }

    private static void marginaliseSeparators(JunctionTreeData jtd) {
      Arrays.stream(jtd.getSeparators()).forEach(Separator::resetSeparator);
    }

  static void writeObservations(JunctionTreeData data) {
    log.info("WRITING OBSERVATIONS...");

    data.getBayesianNetworkData().setObserved(data.getObserved());

    Arrays.stream(data.getCliques())
        .map(Clique::getWriteToObserved)
        .flatMap(Collection::stream)
        .forEach(TransferIterator::setToUnityAndTransfer);

    data.getBayesianNetworkData()
        .getObservationMap()
        .values()
        .forEach(ProbabilityTable::marginalizeTable);

    data.getNodes().forEach(node -> updateTableName(node, data));

    log.info("...OBSERVATIONS WRITTEN!");
  }

  private static void updateTableName(Node node, JunctionTreeData data) {
    MarginalTable observedTable = data.getObservationMap().get(node);
    Collection<NodeState> states = data.getObserved().values();
    StringBuilder sb = new StringBuilder("P(").append(node.getId().toString());
    if (!states.isEmpty()) {
      sb.append("|");
      states.forEach(observed -> sb.append(observed.getId().toString()).append(","));
      sb.deleteCharAt(sb.length() - 1);
    }
    sb.append(")");
    observedTable.setTableName(sb.toString());
  }

  static void writeToNetwork(JunctionTreeData data) {
    log.info("WRITING TO NETWORK");

    BayesianNetworkData bnd = data.getBayesianNetworkData();

    Stream.concat(
            Arrays.stream(data.getCliques()).flatMap(c -> c.getWriteToCPTs().stream()),
            Arrays.stream(data.getCliques()).flatMap(c -> c.getWriteToObserved().stream()))
        .forEach(TransferIterator::transfer);

    Stream.concat(
            bnd.getNetworkTablesMap().values().stream(), bnd.getObservationMap().values().stream())
        .forEach(ProbabilityTable::marginalizeTable);

    log.info("NETWORK TABLES WRITTEN");
  }
}
