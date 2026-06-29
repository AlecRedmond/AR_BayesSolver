package io.github.alecredmond.internal.method.inference.junctiontree;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.method.probabilitytables.TableQueryTool;
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
      multiplyInFromCPTs(clique);
      clique.normalizeTable();
    }
    backupUnobservedData();
    resetSeparators();
  }

  private void setProbabilitiesToUnity(Clique clique) {
    Arrays.fill(clique.getTable().getProbabilities(), 1.0);
  }

  private void multiplyInFromCPTs(Clique clique) {
    clique.getWriteFromCPTs().forEach(TableTransfer::transfer);
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
        .parallel()
        .forEach(TableTransfer::transfer);

    jtd.getObservedTablesMap().values().parallelStream()
        .map(ProbabilityTable::getQueryTool)
        .forEach(TableQueryTool::normalizeTable);

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

    BayesianNetworkData bnd = jtd.getNetworkData();

    Arrays.stream(jtd.getCliques())
        .flatMap(c -> c.getWriteToCPTs().stream())
        .parallel()
        .forEach(TableTransfer::transfer);

    bnd.getNetworkTablesMap().values().parallelStream()
        .map(ProbabilityTable::getQueryTool)
        .forEach(TableQueryTool::normalizeTable);
  }
}
