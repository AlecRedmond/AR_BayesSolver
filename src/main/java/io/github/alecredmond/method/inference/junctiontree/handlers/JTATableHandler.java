package io.github.alecredmond.method.inference.junctiontree.handlers;

import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;
import io.github.alecredmond.method.probabilitytables.probabilityvector.VectorCombinationKeyFactory;
import java.util.*;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;
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

    VectorCombinationKey observedKey =
        new VectorCombinationKeyFactory().buildKey(table, evidenceInTable);

    boolean[] evidenceLock = observedKey.getInnerLock();
    int[] evidencePositions = observedKey.getTumblerKey();
    boolean[] nonEvidenceLock = observedKey.getOuterLock();
    int[] positionKey = new int[nonEvidenceLock.length];

    ProbabilityVector vector = table.getVector();

    iterator.iterateKeyCombos(
        vector,
        positionKey,
        nonEvidenceLock,
        (outerKey, outerIndex) -> {
          boolean isEvidence = checkIsEvidence(outerKey, evidencePositions, evidenceLock);
          ObjIntConsumer<int[]> consumer =
              isEvidence
                  ? ((key, index) -> observed[index] = backup[index])
                  : ((key, index) -> observed[index] = 0.0);
          iterator.iterateKeyCombos(vector, positionKey, evidenceLock, consumer);
        });
  }

  public boolean checkIsEvidence(
      int[] positionCycler, int[] evidencePositions, boolean[] evidenceLock) {
    return IntStream.range(0, positionCycler.length)
        .filter(i -> evidenceLock[i])
        .allMatch(i -> positionCycler[i] == evidencePositions[i]);
  }

  public ProbabilityVector getVector() {
    return table.getVector();
  }
}
