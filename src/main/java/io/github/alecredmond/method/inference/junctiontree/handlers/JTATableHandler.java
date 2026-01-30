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

    if (!isObserved) {
      return;
    }

    double[] probabilities = table.getUnobservedVector().getProbabilities();
    double[] observed = table.getObservedVector().getProbabilities();

    VectorCombinationKey observedKey =
        new VectorCombinationKeyFactory().buildKey(table, evidenceInTable);

    boolean[] evidenceLock = observedKey.getPositionLocked();
    int[] evidencePositions = observedKey.getTumblerKey();
    boolean[] nonEvidenceLock = observedKey.getInvertedLock();
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
                  ? ((key, index) -> observed[index] = probabilities[index])
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

  public double sumProbabilities(VectorCombinationKey comboKey) {
    return table.getUtils().sumProbabilities(comboKey);
  }

  public void marginalize() {
    table.getUtils().marginalizeTable();
  }

  public ProbabilityVector getVector() {
    return table.getVector();
  }

  protected double getRatio(double targetProb, double actualProb) {
    return actualProb == 0 ? 0.0 : targetProb / actualProb;
  }

  protected void adjustToRatio(
      VectorCombinationKey eventKey,
      VectorCombinationKey conditionKey,
      double ratioIfEvent,
      double ratioOtherwise) {
    double[] probabilities = table.getVector().getProbabilities();
    iterateOverConditions(
        eventKey,
        conditionKey,
        (key, index) -> probabilities[index] = ratioIfEvent * probabilities[index],
        (key, index) -> probabilities[index] = ratioOtherwise * probabilities[index]);
  }

  protected void iterateOverConditions(
      VectorCombinationKey eventKey,
      VectorCombinationKey conditionKey,
      ObjIntConsumer<int[]> ifIsEvent,
      ObjIntConsumer<int[]> ifNotEvent) {
    int[] eventPosition = eventKey.getTumblerKey();
    int[] conditionPosition = conditionKey.getTumblerKey();
    boolean[] conditionLock = conditionKey.getPositionLocked();
    boolean[] eventLock = eventKey.getPositionLocked();
    ProbabilityVector vector = table.getVector();

    iterator.iterateKeyCombos(
        vector,
        conditionPosition,
        conditionLock,
        (outerKey, outerIndex) -> {
          boolean isEvent = checkIsEvidence(outerKey, eventPosition, eventLock);
          ObjIntConsumer<int[]> consumer = isEvent ? ifIsEvent : ifNotEvent;
          iterator.iterateKeyCombos(vector, outerKey, eventLock, consumer);
        });
  }
}
