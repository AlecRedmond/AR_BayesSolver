package io.github.alecredmond.internal.method.inference.junctiontree.handlers;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.VectorCombinationKeyFactory;

import java.util.Set;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.ObjIntConsumer;

public class JTAConstraintHandlerConditional extends JTAConstraintHandler {

  public JTAConstraintHandlerConditional(
      JTATableHandler jtaTableHandler, ConditionalConstraint constraint) {
    super(jtaTableHandler, constraint);
  }

  @Override
  protected VectorCombinationKey buildConditionKey() {
    VectorCombinationKey key =
        new VectorCombinationKeyFactory()
            .buildKey(tableHandler.getTable(), constraint.getConditionStates());

    lockAllNonEventNodes(key);
    return key;
  }

  private void lockAllNonEventNodes(VectorCombinationKey key) {
    Node[] nodes = tableHandler.getVector().getNodeArray();
    boolean[] iterateEvents = key.getIterateEvents();
    Set<Node> eventNodes = constraint.getEventNodes();
    for (int i = 0; i < nodes.length; i++) {
      iterateEvents[i] = !eventNodes.contains(nodes[i]);
    }
  }

  @Override
  protected void calculateProbability(
      DoubleAdder eventJointProb, DoubleAdder complementJointProb, DoubleAdder conditionJointProb) {
    double[] probabilities = tableHandler.getVector().getProbabilities();
    iterateOverConditions(
        eventKey,
        conditionKey,
        (key, index) -> {
          double p = probabilities[index];
          eventJointProb.add(p);
          conditionJointProb.add(p);
        },
        (key, index) -> {
          double p = probabilities[index];
          complementJointProb.add(p);
          conditionJointProb.add(p);
        });
  }

  protected void adjustToRatio(double ratioIfEvent, double ratioOtherwise) {
    double[] probabilities = tableHandler.getVector().getProbabilities();
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
    int[] eventPosition = eventKey.getStateIndexes();
    int[] conditionPosition = conditionKey.getStateIndexes();
    boolean[] iterateConditions = conditionKey.getIterateEvents();
    boolean[] iterateEvents = eventKey.getIterateEvents();
    ProbabilityVector vector = tableHandler.getVector();

    iterator.iterateKeyCombos(
        vector,
        conditionPosition,
        iterateConditions,
        (outerKey, outerIndex) -> {
          boolean isEvent = checkIsEvidence(outerKey, eventPosition, iterateEvents);
          ObjIntConsumer<int[]> consumer = isEvent ? ifIsEvent : ifNotEvent;
          iterator.iterateKeyCombos(vector, outerKey, iterateEvents, consumer);
        });
  }
}
