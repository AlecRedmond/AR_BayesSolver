package io.github.alecredmond.internal.method.probabilitytables.tablehelpers;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.JunctionTableSummer;
import java.util.Map;

public class JunctionTreeTableHelperImpl extends TableHelperBase<JunctionTreeTable>
    implements JunctionTreeTableHelper {
  private final JunctionTableSummer summer;

  public JunctionTreeTableHelperImpl(JunctionTreeTable table) {
    super(table);
    summer = new JunctionTableSummer(table);
  }

  @Override
  public void marginalizeTable() {
    TableUtils.marginalizeJointTable(table);
  }

  @Override
  public double sumProbabilities(Map<Node, NodeState> request) {
    return summer.sum(request);
  }
}
