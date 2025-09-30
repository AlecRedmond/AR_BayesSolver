package com.artools.method.jtahandlers;

import com.artools.application.constraints.ParameterConstraint;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ConstraintHandler {
  protected static final double EPSILON = 1E-9;
  protected final JunctionTableHandler junctionTableHandler;
  protected final ParameterConstraint constraint;
  protected List<Integer> constraintIndexes;
  protected List<Integer> conditionIndexes;
  protected List<Integer> complementIndexes;

  protected ConstraintHandler(JunctionTableHandler junctionTableHandler, ParameterConstraint constraint) {
    this.junctionTableHandler = junctionTableHandler;
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
    double eventProb = JunctionTableHandler.getRatio(eventJointProb, conditionProb);

    if (tableLockOccurred(expectedProb, eventProb)) {
      eventProb = preventTableLock();
      conditionProb = eventProb;
    }

    double ratio = JunctionTableHandler.getRatio(expectedProb, eventProb);
    double complementSum = getComplementProb(conditionProb);
    double compRatio = JunctionTableHandler.getRatio((1 - expectedProb), complementSum);
    adjustTable(ratio, compRatio);
    return Math.pow(eventProb - expectedProb, 2);
  }

  protected double getEventJointProb() {
    return junctionTableHandler.sumFromTableIndexes(constraintIndexes);
  }

  protected abstract double getConditionProb();

  private boolean tableLockOccurred(double expectedProb, double eventProb) {
    return expectedProb != 0.0 && eventProb == 0.0;
  }

  protected double preventTableLock() {
    double[] probs = junctionTableHandler.getProbabilities();
    double newProb = 0.0;
    for (int index : constraintIndexes) {
      probs[index] = EPSILON;
      newProb += EPSILON;
    }
    return newProb;
  }

  protected double getComplementProb(double conditionProb) {
    return junctionTableHandler.sumFromTableIndexes(complementIndexes) / conditionProb;
  }

  protected void adjustTable(double ratio, double compRatio) {
    double[] probs = junctionTableHandler.getProbabilities();
    constraintIndexes.forEach(i -> probs[i] = probs[i] * ratio);
    complementIndexes.forEach(i -> probs[i] = probs[i] * compRatio);
  }
}
