package io.github.alecredmond.internal.method.constraints.handlers;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.VectorCombinationKeyFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class JTAConstraintHandler implements ConstraintHandler {
  protected final JTATableHandler tableHandler;
  protected final ProbabilityConstraint constraint;
  protected final ProbabilityVectorIterator iterator;
  protected VectorCombinationKey eventKey;
  protected VectorCombinationKey conditionKey;
  protected List<Double> errors;

  protected JTAConstraintHandler(JTATableHandler tableHandler, ProbabilityConstraint constraint) {
    this.tableHandler = tableHandler;
    this.constraint = constraint;
    this.iterator = new ProbabilityVectorIterator();
    this.eventKey = buildEventKey();
    this.conditionKey = buildConditionKey();
    this.errors = new ArrayList<>();
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
    double error = Math.pow(actualProb - expectedProb, 2);
    errors.add(error);
    return error;
  }

  protected abstract void calculateProbability(
      DoubleAdder eventJointProb, DoubleAdder complementJointProb, DoubleAdder conditionJointProb);

  protected double getRatio(double targetProb, double actualProb) {
    return actualProb == 0 ? 0.0 : targetProb / actualProb;
  }

  protected abstract void adjustToRatio(double ratioIfEvent, double ratioOtherwise);

  public void updateResults(Map<ProbabilityConstraint, double[]> results) {
    if (results.containsKey(constraint)) {
      double[] existing = results.get(constraint);
      double thisSum = errors.stream().mapToDouble(Double::doubleValue).sum();
      double existingSum = Arrays.stream(existing).sum();
      if (existingSum >= thisSum) return;
    }
    double[] newArray = errors.stream().mapToDouble(Double::doubleValue).toArray();
    results.put(constraint, newArray);
  }

  protected boolean checkIsEvidence(
      int[] odometerKey, int[] evidencePositions, boolean[] evidenceLock) {
    return IntStream.range(0, odometerKey.length)
        .filter(i -> evidenceLock[i])
        .allMatch(i -> odometerKey[i] == evidencePositions[i]);
  }
}
