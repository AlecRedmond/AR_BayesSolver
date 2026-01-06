package io.github.alecredmond.method.inference.junctiontree.handlers;

import io.github.alecredmond.application.constraints.ParameterConstraint;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class JTAConstraintHandler {
  protected static final double TABLE_LOCK_EPSILON = 1E-9;
  protected final JTATableHandler jtaTableHandler;
  protected final ParameterConstraint constraint;
  protected List<Integer> constraintIndexes;
  protected List<Integer> conditionIndexes;
  protected List<Integer> complementIndexes;

  protected JTAConstraintHandler(JTATableHandler jtaTableHandler, ParameterConstraint constraint) {
    this.jtaTableHandler = jtaTableHandler;
    this.constraint = constraint;
    Set<Integer> conditionIndexSet = getConditionIndexSet();
    this.conditionIndexes = conditionIndexSet.stream().toList();
    Set<Integer> constraintIndexSet = getConstraintIndexeSet(conditionIndexSet);
    this.complementIndexes = getComplementIndexList(constraintIndexSet, conditionIndexSet);
    this.constraintIndexes = constraintIndexSet.stream().toList();
  }

  protected abstract Set<Integer> getConditionIndexSet();

  protected abstract Set<Integer> getConstraintIndexeSet(Set<Integer> conditionIndexSet);

  protected abstract List<Integer> getComplementIndexList(
      Set<Integer> constraintIndexSet, Set<Integer> conditionIndexSet);

  public double adjustAndReturnError() {
    double expectedProb = constraint.getProbability();
    double eventJointProb = getEventJointProb();
    double conditionProb = getConditionProb();
    double eventProb = JTATableHandler.getRatio(eventJointProb, conditionProb);

    if (tableLockOccurred(expectedProb, eventProb)) {
      eventProb = preventTableLock();
      conditionProb = eventProb;
    }

    double ratio = JTATableHandler.getRatio(expectedProb, eventProb);
    double complementSum = getComplementProb(conditionProb);
    double compRatio = JTATableHandler.getRatio((1 - expectedProb), complementSum);
    adjustTable(ratio, compRatio);
    return Math.pow(eventProb - expectedProb, 2);
  }

  protected double getEventJointProb() {
    return jtaTableHandler.sumFromTableIndexes(constraintIndexes);
  }

  protected abstract double getConditionProb();

  private boolean tableLockOccurred(double expectedProb, double eventProb) {
    return expectedProb != 0.0 && eventProb == 0.0;
  }

  protected double preventTableLock() {
    double[] probs = jtaTableHandler.getProbabilities();
    double newProb = 0.0;
    for (int index : constraintIndexes) {
      probs[index] = TABLE_LOCK_EPSILON;
      newProb += TABLE_LOCK_EPSILON;
    }
    return newProb;
  }

  protected double getComplementProb(double conditionProb) {
    return jtaTableHandler.sumFromTableIndexes(complementIndexes) / conditionProb;
  }

  protected void adjustTable(double ratio, double compRatio) {
    double[] probs = jtaTableHandler.getProbabilities();
    constraintIndexes.forEach(i -> probs[i] = probs[i] * ratio);
    complementIndexes.forEach(i -> probs[i] = probs[i] * compRatio);
  }
}
