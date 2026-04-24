package io.github.alecredmond.internal.method.inference.junctiontree.handlers;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.ObservationCopier;
import java.util.*;
import lombok.Getter;

@Getter
public class JTATableHandler {
  protected final JunctionTreeTable table;
  protected final ObservationCopier copier;

  // TODO - fold into JunctionTreeHelperImpl
  public JTATableHandler(JunctionTreeTable table) {
    this.table = table;
    this.copier = new ObservationCopier(table);
  }

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

  public void resetObservations() {
    table.getObservedStates().clear();
    writeFromBackup(new HashSet<>());
  }

  public ProbabilityVector getVector() {
    return table.getVector();
  }
}
