package io.github.alecredmond.internal.method.constraints.strategies.baseobjects;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.BaseVectorIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseConstraintSolverHandler extends BaseVectorIterator {
  protected final JTATableHandler tableHandler;
  protected final ProbabilityConstraint constraint;
  protected List<Double> errors;

  protected BaseConstraintSolverHandler(
      JTATableHandler tableHandler,
      ProbabilityConstraint constraint,
      VectorOdometer vectorOdometer) {
    super(vectorOdometer);
    this.tableHandler = tableHandler;
    this.constraint = constraint;
    this.errors = new ArrayList<>();
  }

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

  private void calculateProbability(
      DoubleAdder eventJointProb, DoubleAdder complementJointProb, DoubleAdder conditionJointProb) {
    int[] stateIndexes = vectorOdometer.getStateIndexes();
    boolean[][] stateIsEvent = vectorOdometer.getNodeStateEvidenceArray();
    double[] probs = vectorOdometer.getProbabilities();

    iterateOuter(
        () -> {
          boolean isEvidence = checkIsEvidence(stateIndexes, stateIsEvent);
          DoubleAdder correctAdder = isEvidence ? eventJointProb : complementJointProb;
          iterateInner(
              (o, i) -> {
                double prob = probs[i];
                conditionJointProb.add(prob);
                correctAdder.add(prob);
              });
        });
  }

  protected double getRatio(double targetProb, double actualProb) {
    return actualProb == 0 ? 0.0 : targetProb / actualProb;
  }

  protected void adjustToRatio(double ratioIfEvent, double ratioOtherwise) {
    int[] stateIndexes = vectorOdometer.getStateIndexes();
    boolean[][] stateIsEvent = vectorOdometer.getNodeStateEvidenceArray();
    double[] probs = vectorOdometer.getProbabilities();

    iterateOuter(
        () -> {
          double ratio =
              checkIsEvidence(stateIndexes, stateIsEvent) ? ratioIfEvent : ratioOtherwise;
          iterateInner((o, i) -> probs[i] = probs[i] * ratio);
        });
  }

  protected boolean checkIsEvidence(int[] stateIndexes, boolean[][] stateIsEvent) {
    return IntStream.range(0, stateIsEvent.length)
        .filter(x -> stateIsEvent[x].length != 0)
        .allMatch(x -> stateIsEvent[x][stateIndexes[x]]);
  }

  public void storeError(double error) {
    errors.add(error);
  }

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

  public void performRun() {
    // UNUSED FOR CONSTRAINT SOLVER HANDLERS
  }
}
