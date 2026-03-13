package io.github.alecredmond.internal.method.inference.junctiontree.handlers;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import java.util.Arrays;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.ObjIntConsumer;

public class JTAConstraintHandlerMarginal extends JTAConstraintHandler {
  private final int[] positionKey;

  public JTAConstraintHandlerMarginal(
      JTATableHandler jtaTableHandler, MarginalConstraint constraint) {
    super(jtaTableHandler, constraint);
    this.positionKey = new int[eventKey.getStateIndexes().length];
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
    int[] eventPosition = eventKey.getStateIndexes();
    boolean[] iterateEvents = eventKey.getIterateEvents();
    boolean[] iterateConditions = eventKey.getIterateConditions();
    ProbabilityVector vector = tableHandler.getVector();

    iterator.iterateKeyCombos(
        vector,
        positionKey,
        iterateConditions,
        (outerKey, outerIndex) -> {
          boolean isEvent = checkIsEvidence(outerKey, eventPosition, iterateEvents);
          ObjIntConsumer<int[]> consumer = isEvent ? ifIsEvent : ifNotEvent;
          iterator.iterateKeyCombos(vector, outerKey, iterateEvents, consumer);
        });
  }
}
