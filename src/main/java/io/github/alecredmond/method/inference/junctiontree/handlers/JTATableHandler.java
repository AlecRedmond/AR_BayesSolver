package io.github.alecredmond.method.inference.junctiontree.handlers;

import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;
import io.github.alecredmond.method.probabilitytables.probabilityvector.VectorCombinationKeyFactory;
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

  public void setObserved(Set<NodeState> evidenceInTable, boolean isObserved) {
    table.setObserved(isObserved);
    table.getObservedStates().clear();
    table.getObservedStates().addAll(evidenceInTable);

    double[] backup = table.getBackupVector().getProbabilities();
    double[] observed = table.getVector().getProbabilities();

    if (!isObserved) {
      System.arraycopy(backup, 0, observed, 0, backup.length);
      return;
    }

    Arrays.fill(observed, 0.0);

    VectorCombinationKey observedKey =
        new VectorCombinationKeyFactory().buildKey(table, evidenceInTable);

    ProbabilityVector vector = table.getVector();

    iterator.iterateKeyCombos(
        vector, observedKey, (k, i) -> observed[i] = backup[i]);
  }

    public ProbabilityVector getVector() {
    return table.getVector();
  }
}
