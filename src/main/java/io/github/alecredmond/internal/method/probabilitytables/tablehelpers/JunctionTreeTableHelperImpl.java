package io.github.alecredmond.internal.method.probabilitytables.tablehelpers;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.JunctionTableSummer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.ObservationCopier;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JunctionTreeTableHelperImpl extends TableHelperBase<JunctionTreeTable>
    implements JunctionTreeTableHelper {
  private JunctionTableSummer summer;
  private ObservationCopier copier;

  public JunctionTreeTableHelperImpl(JunctionTreeTable table) {
    super(table);
  }

  public void initHelper() {
    this.summer = new JunctionTableSummer(table);
    this.copier = new ObservationCopier(table);
  }

  @Override
  public void marginalizeTable() {
    TableUtils.marginalizeJointTable(table);
  }

  @Override
  public Map<NodeState, Double> getConditionalProb(Collection<NodeState> condition) {
    return Map.of();
  }

  @Override
  public Map<NodeState, Double> getConditionalProbByIds(Collection<Serializable> conditionIDs) {
    return Map.of();
  }

  @Override
  public double sumProbabilities(Collection<NodeState> states) {
    return summer.sum(states);
  }

  @Override
  public void setObserved(Set<NodeState> evidenceInTable) {
    table.getObservedStates().clear();
    table.getObservedStates().addAll(evidenceInTable);
    writeFromBackup(evidenceInTable);
  }

  private void writeFromBackup(Set<NodeState> evidenceInTable) {
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
    table.getObservedStates().clear();
    writeFromBackup(new HashSet<>());
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
