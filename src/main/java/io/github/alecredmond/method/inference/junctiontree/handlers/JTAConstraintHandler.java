package io.github.alecredmond.method.inference.junctiontree.handlers;

import io.github.alecredmond.application.constraints.ParameterConstraint;
import io.github.alecredmond.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class JTAConstraintHandler {
  protected final JTATableHandler tableHandler;
  protected final ParameterConstraint constraint;
  protected final ProbabilityVectorIterator iterator;
  protected VectorCombinationKey eventKey;
  protected VectorCombinationKey conditionKey;

  protected JTAConstraintHandler(JTATableHandler tableHandler, ParameterConstraint constraint) {
    this.tableHandler = tableHandler;
    this.constraint = constraint;
    this.iterator = new ProbabilityVectorIterator();
  }

  public double adjustAndReturnError() {
    double expectedProb = constraint.getProbability();
    double actualProb = calculateEventProbability();

    double adjustmentRatio = getRatio(expectedProb, actualProb);
    double compRatio = getRatio((1 - expectedProb), (1 - actualProb));
    tableHandler.adjustToRatio(eventKey, conditionKey, adjustmentRatio, compRatio);
    return Math.pow(actualProb - expectedProb, 2);
  }

  protected abstract double calculateEventProbability();

  protected double getRatio(double targetProb, double actualProb) {
    return actualProb == 0 ? 0.0 : targetProb / actualProb;
  }
}
