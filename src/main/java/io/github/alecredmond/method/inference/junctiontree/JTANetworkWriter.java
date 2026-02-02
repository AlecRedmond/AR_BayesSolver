package io.github.alecredmond.method.inference.junctiontree;

import io.github.alecredmond.application.inference.junctiontree.Clique;
import io.github.alecredmond.application.inference.junctiontree.JunctionTreeData;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.method.inference.junctiontree.handlers.readwrite.JTAMessagePasser;
import io.github.alecredmond.method.probabilitytables.TableUtils;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class JTANetworkWriter {

  private JTANetworkWriter() {}

  static void initializeJunctionTreeFromNetwork(JunctionTreeData data) {
    data.getCliqueSet()
        .forEach(
            clique -> {
              setProbabilitiesToUnity(clique);
              clique.getInitializeFrom().forEach(JTAMessagePasser::run);
            });
  }

  private static void setProbabilitiesToUnity(Clique clique) {
    Arrays.fill(clique.getTable().getVector().getProbabilities(), 1.0);
  }

  static void writeObservations(JunctionTreeData data) {
    log.info("WRITING OBSERVATIONS...");

    data.getBayesianNetworkData().setObserved(data.getObserved());

    data.getCliqueSet().stream()
        .map(Clique::getObservationWriteMap)
        .flatMap(map -> map.entrySet().stream())
        .map(Map.Entry::getValue)
        .forEach(JTAMessagePasser::setToUnityAndRun);

    data.getBayesianNetworkData().getObservationMap().values().stream()
        .map(ProbabilityTable::getUtils)
        .forEach(TableUtils::marginalizeTable);

    data.getNodes().forEach(node -> updateTableName(node, data));

    log.info("...OBSERVATIONS WRITTEN!");
  }

  private static void updateTableName(Node node, JunctionTreeData data) {
    MarginalTable observedTable = data.getObservationMap().get(node);
    Collection<NodeState> states = data.getObserved().values();
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

    data.getCliqueSet().stream()
        .flatMap(c -> c.getNetworkWriteMap().entrySet().stream())
        .map(Map.Entry::getValue)
        .forEach(JTAMessagePasser::setToUnityAndRun);

    data.getBayesianNetworkData().getNetworkTablesMap().values().stream()
        .map(ProbabilityTable::getUtils)
        .forEach(TableUtils::marginalizeTable);

    log.info("NETWORK TABLES WRITTEN");
  }
}
