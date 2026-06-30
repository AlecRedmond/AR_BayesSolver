package io.github.alecredmond.internal.method.probabilitytables.tablehelpers;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.JunctionTreeTableBuilder;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilder;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.JunctionTableSummer;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.ObservationCopier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class JunctionTreeTableQueryToolImpl extends QueryToolBase<JunctionTreeTable>
    implements JunctionTreeTableQueryTool {
  private final JunctionTableSummer summer;
  private final ObservationCopier copier;

  public JunctionTreeTableQueryToolImpl(JunctionTreeTable table) {
    super(table);
    this.summer = new JunctionTableSummer(table);
    this.copier = new ObservationCopier(table);
  }

  @Override
  public void normalizeTable() {
    TableUtils.marginalizeJointTable(table);
  }

  @Override
  public double sumProbabilities(Collection<NodeState> states) {
    return summer.sum(states);
  }

  @Override
  protected Supplier<TableBuilder<JunctionTreeTable>> supplyTableBuilder() {
    return JunctionTreeTableBuilder::new;
  }

  @Override
  public void setObserved(Set<NodeState> evidenceInTable) {
    if (!evidenceInTable.isEmpty()) {
      copier.observeTable(evidenceInTable);
      return;
    }
    double[] backup = table.getBackupVector().getProbabilities();
    double[] observed = table.getVector().getProbabilities();
    System.arraycopy(backup, 0, observed, 0, backup.length);
  }

  @Override
  public void resetObservations() {
    setObserved(new HashSet<>());
  }

  @Override
  public ProbabilityVector getVector() {
    return table.getVector();
  }

  @Override
  public JunctionTreeTable getTable() {
    return table;
  }
}
