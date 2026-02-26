package io.github.alecredmond.method.inference.junctiontree.handlers;

import io.github.alecredmond.application.constraints.MarginalConstraint;
import io.github.alecredmond.application.probabilitytables.export.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.internal.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.method.probabilitytables.TableUtils;
import java.util.Arrays;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.ObjIntConsumer;

public class JTAConstraintHandlerMarginal extends JTAConstraintHandler {
  private final int[] positionKey;

  public JTAConstraintHandlerMarginal(
      JTATableHandler jtaTableHandler, MarginalConstraint constraint) {
    super(jtaTableHandler, constraint);
    this.positionKey = new int[eventKey.getStateKey().length];
  }

  @Override
  protected VectorCombinationKey buildConditionKey() {
    return null;
  }

  @Override
  protected void calculateProbability(
      DoubleAdder eventJointProb, DoubleAdder complementJointProb, DoubleAdder conditionJointProb) {
    eventJointProb.add(TableUtils.sumProbabilities(eventKey, tableHandler.getTable()));
    complementJointProb.add(
        Arrays.stream(tableHandler.getVector().getProbabilities()).sum() - eventJointProb.sum());
    conditionJointProb.add(1.0);
  }

  @Override
  protected void adjustToRatio(double ratioIfEvent, double ratioOtherwise) {
    double[] probabilities = tableHandler.getVector().getProbabilities();
    iterateOverConditions(
        (key, index) -> probabilities[index] = ratioIfEvent * probabilities[index],
        (key, index) -> probabilities[index] = ratioOtherwise * probabilities[index]);
  }

  private void iterateOverConditions(
      ObjIntConsumer<int[]> ifIsEvent, ObjIntConsumer<int[]> ifNotEvent) {
    int[] eventPosition = eventKey.getStateKey();
    boolean[] innerLock = eventKey.getInnerLock();
    boolean[] outerLock = eventKey.getOuterLock();
    ProbabilityVector vector = tableHandler.getVector();

    iterator.iterateKeyCombos(
        vector,
        positionKey,
        outerLock,
        (outerKey, outerIndex) -> {
          boolean isEvent = checkIsEvidence(outerKey, eventPosition, innerLock);
          ObjIntConsumer<int[]> consumer = isEvent ? ifIsEvent : ifNotEvent;
          iterator.iterateKeyCombos(vector, outerKey, innerLock, consumer);
        });
  }
}
