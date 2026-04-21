package io.github.alecredmond.internal.method.inference.junctiontree.handlers;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory.ObservationCopierFactory;
import java.util.*;
import lombok.Getter;

@Getter
public class JTATableHandler {
  protected final JunctionTreeTable table;
  protected final ObservationCopierFactory copierFactory;

  public JTATableHandler(JunctionTreeTable table) {
    this.table = table;
    this.copierFactory = new ObservationCopierFactory(table.getVector(), table.getBackupVector());
  }

  public void setObserved(Set<NodeState> evidenceInTable) {
    table.getObservedStates().clear();
    table.getObservedStates().addAll(evidenceInTable);
    writeFromBackup(evidenceInTable);
  }

  private void writeFromBackup(Set<NodeState> evidenceInTable) {
    if (!evidenceInTable.isEmpty()) {
      copierFactory.build(evidenceInTable).performRun();
      return;
    }
    double[] backup = table.getBackupVector().getProbabilities();
    double[] observed = table.getVector().getProbabilities();
    System.arraycopy(backup, 0, observed, 0, backup.length);
  }

  public void resetObservations() {
    table.getObservedStates().clear();
    writeFromBackup(new HashSet<>());
  }

  public ProbabilityVector getVector() {
    return table.getVector();
  }
}
