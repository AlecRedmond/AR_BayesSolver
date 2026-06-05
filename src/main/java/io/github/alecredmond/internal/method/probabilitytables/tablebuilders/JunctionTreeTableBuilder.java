package io.github.alecredmond.internal.method.probabilitytables.tablebuilders;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTableImpl;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.JunctionTreeTableHelperImpl;
import java.util.*;

public class JunctionTreeTableBuilder extends BaseTableBuilder
    implements TableBuilder<JunctionTreeTable> {

  public JunctionTreeTable buildTable(Set<Node> events, BayesianNetworkData bnd) {
    List<Node> orderedNodes = bnd.getNodes().stream().filter(events::contains).toList();
    return buildTable(orderedNodes, new ArrayList<>());
  }

  @Override
  public JunctionTreeTable buildTable(List<Node> events, List<Node> conditions) {
    TableBuilderData data = buildData(events, conditions);
    ProbabilityVector backupVector = buildProbabilityVector(events);
    JunctionTreeTableImpl table =
        new JunctionTreeTableImpl(data, backupVector, new LinkedHashSet<>());
    table.setHelper(new JunctionTreeTableHelperImpl(table));
    return table;
  }

  @Override
  public JunctionTreeTable copyTable(JunctionTreeTable table) {
    return copyTable(table, this::buildTable, this::additionalCopyLogic);
  }

  protected void additionalCopyLogic(JunctionTreeTable original, JunctionTreeTable copied) {
    double[] oldProbBackup = original.getBackupVector().getProbabilities();
    double[] newProbBackup = copied.getBackupVector().getProbabilities();
    System.arraycopy(oldProbBackup, 0, newProbBackup, 0, oldProbBackup.length);
    copied.getObservedStates().addAll(original.getObservedStates());
  }
}
