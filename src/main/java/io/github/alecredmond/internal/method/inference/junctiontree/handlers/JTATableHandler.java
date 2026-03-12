package io.github.alecredmond.internal.method.inference.junctiontree.handlers;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.VectorCombinationKeyFactory;

import java.util.*;
import lombok.Getter;

@Getter
public class JTATableHandler {
  protected final JunctionTreeTable table;
  protected final ProbabilityVectorIterator iterator;

  public JTATableHandler(JunctionTreeTable table) {
    this.table = table;
    this.iterator = new ProbabilityVectorIterator();
  }

  public void setObserved(Set<NodeState> evidenceInTable) {
    table.setObserved(true);
    table.getObservedStates().clear();
    table.getObservedStates().addAll(evidenceInTable);
    writeFromBackup(evidenceInTable);
  }

  private void writeFromBackup(Set<NodeState> evidenceInTable) {
    double[] backup = table.getBackupVector().getProbabilities();
    double[] observed = table.getVector().getProbabilities();

    if (evidenceInTable.isEmpty()) {
      System.arraycopy(backup, 0, observed, 0, backup.length);
      return;
    }

    Arrays.fill(observed, 0.0);

    VectorCombinationKey observedKey =
        new VectorCombinationKeyFactory().buildKey(table, evidenceInTable);

    ProbabilityVector vector = table.getVector();

    iterator.iterateEvents(vector, observedKey, (k, i) -> observed[i] = backup[i]);
  }

  public void resetObservations() {
    table.setObserved(false);
    table.getObservedStates().clear();
    writeFromBackup(new HashSet<>());
  }

  public ProbabilityVector getVector() {
    return table.getVector();
  }
}
