package io.github.alecredmond.internal.method.inference.junctiontree;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;
import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.inference.junctiontree.Separator;
import io.github.alecredmond.internal.application.probabilitytables.ObservedTableImpl;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class JTANetworkWriter {
  private final JunctionTreeData jtd;

  public JTANetworkWriter(JunctionTreeData jtd) {
    this.jtd = jtd;
  }

  public void initializeJunctionTreeFromNetwork() {
    for (Clique clique : jtd.getCliques()) {
      setProbabilitiesToUnity(clique);
      clique.getWriteFromCPTs().forEach(TableTransfer::transfer);
      clique.normalizeTable();
    }
    backupUnobservedData();
    resetSeparators();
  }

  private void setProbabilitiesToUnity(Clique clique) {
    Arrays.fill(clique.getTable().getProbabilities(), 1.0);
  }

  private void backupUnobservedData() {
    Arrays.stream(jtd.getCliques())
        .map(Clique::getTable)
        .forEach(
            jtt -> {
              double[] solvedProbabilities = jtt.getVector().getProbabilities();
              double[] backup = jtt.getBackupVector().getProbabilities();
              System.arraycopy(solvedProbabilities, 0, backup, 0, backup.length);
            });
  }

  private void resetSeparators() {
    Arrays.stream(jtd.getSeparators()).forEach(Separator::resetSeparator);
  }

  public void writeObservations() {
    BayesianNetworkData networkData = jtd.getNetworkData();

    Arrays.stream(jtd.getCliques())
        .map(Clique::getWriteToObserved)
        .flatMap(Collection::stream)
        .forEach(TableTransfer::transfer);

    jtd.getObservedTablesMap().values().stream()
        .map(ProbabilityTable::getHelper)
        .forEach(TableHelper::normalizeTable);

    Map<Node, NodeState> observationMap = Collections.unmodifiableMap(jtd.getObservedEvidence());
    networkData.getNodes().forEach(node -> updateObservedTables(node, observationMap));
  }

  private void updateObservedTables(Node node, Map<Node, NodeState> observationMap) {
    ObservedTableImpl oti = ((ObservedTableImpl) jtd.getObservedTablesMap().get(node));
    oti.setObservations(observationMap);
    oti.setTableName(
        TableUtils.buildTableName(
            List.of(node.getId()), NodeUtils.getNodeStateIds(jtd.getObservedEvidence().values())));
  }

  public void writeBackToCPTs() {
    log.info("WRITING TO NETWORK");

    BayesianNetworkData bnd = jtd.getNetworkData();

    Arrays.stream(jtd.getCliques())
        .flatMap(c -> c.getWriteToCPTs().stream())
        .forEach(TableTransfer::transfer);

    bnd.getNetworkTablesMap().values().stream()
        .map(ProbabilityTable::getHelper)
        .forEach(TableHelper::normalizeTable);

    log.info("NETWORK TABLES WRITTEN");
  }
}
