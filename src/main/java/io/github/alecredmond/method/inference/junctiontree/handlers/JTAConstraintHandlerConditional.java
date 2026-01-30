package io.github.alecredmond.method.inference.junctiontree.handlers;

import io.github.alecredmond.application.constraints.ConditionalConstraint;
import java.util.concurrent.atomic.DoubleAdder;

public class JTAConstraintHandlerConditional extends JTAConstraintHandler {

  public JTAConstraintHandlerConditional(
      JTATableHandler jtaTableHandler, ConditionalConstraint constraint) {
    super(jtaTableHandler, constraint);

  }

  @Override
  protected double calculateEventProbability() {
    DoubleAdder eventJointProbAdder = new DoubleAdder();
    DoubleAdder conditionJointProbAdder = new DoubleAdder();
    double[] probs = tableHandler.getVector().getProbabilities();
    tableHandler.iterateOverConditions(
        eventKey,
        conditionKey,
        (key, index) -> {
          double p = probs[index];
          eventJointProbAdder.add(p);
          conditionJointProbAdder.add(p);
        },
        (key, index) -> conditionJointProbAdder.add(probs[index]));
    double eventJointProb = eventJointProbAdder.sum();
    double conditionJointProb = conditionJointProbAdder.sum();
    return getRatio(eventJointProb, conditionJointProb);
  }
}
