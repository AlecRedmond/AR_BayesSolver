package com.artools.method.solver.netsampler;

import com.artools.application.junctiontree.Clique;
import com.artools.application.junctiontree.JunctionTreeData;
import com.artools.application.junctiontree.Separator;
import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.JunctionTreeTable;
import com.artools.application.probabilitytables.MarginalTable;
import com.artools.application.probabilitytables.ProbabilityTable;
import com.artools.method.probabilitytables.TableUtils;
import java.util.Comparator;
import java.util.Set;

public class NetworkJunctionConverter {
  private final JunctionTreeData data;

  public NetworkJunctionConverter(JunctionTreeData data) {
    this.data = data;
  }

  public void initializeJunctionTreeFromNetwork() {
    data.getAssociatedTables()
        .forEach(
            (clique, tableSet) ->
                clique
                    .getTable()
                    .getKeySet()
                    .forEach(request -> setCliqueProbability(clique, tableSet, request)));

    setSeparatorsToUnity();
  }

  private void setCliqueProbability(
      Clique clique, Set<ProbabilityTable> tableSet, Set<NodeState> request) {
    double prob =
        tableSet.stream()
            .mapToDouble(table -> table.getProbability(request, true))
            .reduce(1.0, (x, y) -> x * y);
    clique.getTable().setProbability(request, prob);
  }

  public void setSeparatorsToUnity() {
    data.getSeparators().stream()
        .map(Separator::getTable)
        .forEach(table -> table.getProbabilitiesMap().replaceAll((k, v) -> 1.0));
  }

  public void writeToObservations() {
    data.getNodes()
        .forEach(
            node -> {
              ProbabilityTable bestTable = getBestTable(node);
              MarginalTable observedTable = data.getObservationMap().get(node);
              TableUtils.fillObservedTable(observedTable, bestTable);
            });
  }

  private JunctionTreeTable getBestTable(Node node) {
    return data.getJunctionTreeTables().stream()
        .filter(table -> table.getNodes().contains(node))
        .min(Comparator.comparingInt(table -> table.getNodes().size()))
        .orElseThrow();
  }

  public void writeToNetwork() {
    data.getAssociatedTables()
        .forEach(
            ((clique, netTables) -> {
              JunctionTreeTable junctionTable = clique.getTable();
              netTables.forEach(
                  nt -> TableUtils.updateNetworkTableFromJunctionTable(junctionTable, nt));
            }));
  }

}
