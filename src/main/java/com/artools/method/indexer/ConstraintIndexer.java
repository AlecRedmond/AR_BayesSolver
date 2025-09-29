package com.artools.method.indexer;

import com.artools.application.constraints.MarginalConstraint;
import com.artools.application.constraints.ParameterConstraint;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ConstraintIndexer {
  protected static final double EPSILON = 1E-9;
  protected static int counter = 0;
  protected final TableIndexer tableIndexer;
  protected final ParameterConstraint constraint;
  protected List<Integer> constraintIndexes;
  protected List<Integer> conditionIndexes;
  protected List<Integer> complementIndexes;

  protected ConstraintIndexer(TableIndexer tableIndexer, ParameterConstraint constraint) {
    this.tableIndexer = tableIndexer;
    this.constraint = constraint;
    Set<Integer> conditionIndexSet = getConditionIndexSet();
    this.conditionIndexes = conditionIndexSet.stream().toList();
    Set<Integer> constraintIndexSet = getConstraintIndexeSet(conditionIndexSet);
    this.complementIndexes = getComplementIndexList(constraintIndexSet, conditionIndexSet);
    this.constraintIndexes = constraintIndexSet.stream().toList();
    String type = constraint instanceof MarginalConstraint ? "Marginal" : "Conditional";
    String constraintToString =
        constraint.getAllStates().stream()
            .map(ns -> ns.toString() + " ")
            .collect(Collectors.joining());
    log.info(String.format("%s constraint %d built %s", type, counter, constraintToString));
    counter++;
  }

  protected abstract Set<Integer> getConditionIndexSet();

  protected abstract Set<Integer> getConstraintIndexeSet(Set<Integer> conditionIndexSet);

  protected abstract List<Integer> getComplementIndexList(
      Set<Integer> constraintIndexSet, Set<Integer> conditionIndexSet);

  public double adjustAndReturnError() {
    double expectedProb = constraint.getProbability();
    double eventJointProb = getEventJointProb();
    double conditionProb = getConditionProb();
    double eventProb = TableIndexer.getRatio(eventJointProb, conditionProb);

    if (tableLockOccurred(expectedProb, eventProb)) {
      eventProb = preventTableLock();
      conditionProb = eventProb;
    }

    double ratio = TableIndexer.getRatio(expectedProb, eventProb);
    double complementSum = getComplementProb(conditionProb);
    double compRatio = TableIndexer.getRatio((1 - expectedProb), complementSum);
    adjustTable(ratio, compRatio);
    return Math.pow(eventProb - expectedProb, 2);
  }

  protected double getEventJointProb() {
    return tableIndexer.sumFromTableIndexes(constraintIndexes);
  }

  protected abstract double getConditionProb();

  private boolean tableLockOccurred(double expectedProb, double eventProb) {
    return expectedProb != 0.0 && eventProb == 0.0;
  }

  protected double preventTableLock() {
    double[] probs = tableIndexer.getProbabilities();
    double newProb = 0.0;
    for (int index : constraintIndexes) {
      probs[index] = EPSILON;
      newProb += EPSILON;
    }
    return newProb;
  }

  protected double getComplementProb(double conditionProb) {
    return tableIndexer.sumFromTableIndexes(complementIndexes) / conditionProb;
  }

  protected void adjustTable(double ratio, double compRatio) {
    double[] probs = tableIndexer.getProbabilities();
    constraintIndexes.forEach(i -> probs[i] = probs[i] * ratio);
    complementIndexes.forEach(i -> probs[i] = probs[i] * compRatio);
  }
}
