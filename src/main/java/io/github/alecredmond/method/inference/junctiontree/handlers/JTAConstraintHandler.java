package io.github.alecredmond.method.inference.junctiontree.handlers;

import io.github.alecredmond.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.application.probabilitytables.internal.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;
import io.github.alecredmond.method.probabilitytables.probabilityvector.VectorCombinationKeyFactory;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class JTAConstraintHandler {
  protected final JTATableHandler tableHandler;
  protected final ProbabilityConstraint constraint;
  protected final ProbabilityVectorIterator iterator;
  protected VectorCombinationKey eventKey;
  protected VectorCombinationKey conditionKey;

  protected JTAConstraintHandler(JTATableHandler tableHandler, ProbabilityConstraint constraint) {
    this.tableHandler = tableHandler;
    this.constraint = constraint;
    this.iterator = new ProbabilityVectorIterator();
    this.eventKey = buildEventKey();
    this.conditionKey = buildConditionKey();
  }

  protected VectorCombinationKey buildEventKey() {
    return new VectorCombinationKeyFactory()
        .buildKey(tableHandler.getTable(), constraint.getAllStates());
  }

  protected abstract VectorCombinationKey buildConditionKey();

  public double adjustAndReturnError() {
    double expectedProb = constraint.getProbability();
    DoubleAdder eventJointProb = new DoubleAdder();
    DoubleAdder conditionJointProb = new DoubleAdder();
    DoubleAdder complementJointProb = new DoubleAdder();
    calculateProbability(eventJointProb, complementJointProb, conditionJointProb);

    double actualProb = getRatio(eventJointProb.sum(), conditionJointProb.sum());
    double complementProb = getRatio(complementJointProb.sum(), conditionJointProb.sum());

    double adjustmentRatio = getRatio(expectedProb, actualProb);
    double compRatio = getRatio((1 - expectedProb), complementProb);

    adjustToRatio(adjustmentRatio, compRatio);
    return Math.pow(actualProb - expectedProb, 2);
  }

  protected abstract void calculateProbability(
      DoubleAdder eventJointProb, DoubleAdder complementJointProb, DoubleAdder conditionJointProb);

  protected double getRatio(double targetProb, double actualProb) {
    return actualProb == 0 ? 0.0 : targetProb / actualProb;
  }

  protected abstract void adjustToRatio(double ratioIfEvent, double ratioOtherwise);

  protected boolean checkIsEvidence(
      int[] positionCycler, int[] evidencePositions, boolean[] evidenceLock) {
    return IntStream.range(0, positionCycler.length)
        .filter(i -> evidenceLock[i])
        .allMatch(i -> positionCycler[i] == evidencePositions[i]);
  }
}
