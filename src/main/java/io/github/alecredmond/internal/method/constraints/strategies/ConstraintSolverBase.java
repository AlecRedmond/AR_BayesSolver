package io.github.alecredmond.internal.method.constraints.strategies;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.BaseOdometerResetLogic;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.updatelogictypes.BlankUpdater;
import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConstraintSolverBase
    implements BaseOdometerResetLogic, BlankUpdater, ConstraintSolver {
  protected final VectorIterator iterator;
  protected final ProbabilityConstraint constraint;
  protected final DoubleAdder eventJointProb = new DoubleAdder();
  protected final DoubleAdder conditionJointProb = new DoubleAdder();
  protected final DoubleAdder complementJointProb = new DoubleAdder();
  protected final List<Double> errors = new ArrayList<>();

  public ConstraintSolverBase(ProbabilityConstraint constraint, JunctionTreeTable table) {
    this.constraint = constraint;
    this.iterator = new VectorIterator(table.getVector(), this, VectorOdometer::new);
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    Map<Node, NodeState> condMap = NodeUtils.generateRequest(constraint.getConditionStates());
    return node -> condMap.containsKey(node) ? condMap.get(node) : node.getNodeStates().getFirst();
  }

  @Override
  public Function<Node, boolean[]> buildEvidenceMaps() {
    Set<Node> eventNodes = constraint.getEventNodes();
    Set<NodeState> eventStates = constraint.getEventStates();
    return node -> {
      if (!eventNodes.contains(node)) {
        return new boolean[0];
      }
      List<NodeState> states = node.getNodeStates();
      boolean[] isEvidence = new boolean[states.size()];
      IntStream.range(0, states.size())
          .filter(y -> eventStates.contains(states.get(y)))
          .forEach(y -> isEvidence[y] = true);
      return isEvidence;
      // Java pls give BooleanStream functions T^T
    };
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    Set<Node> events = constraint.getEventNodes();
    return node -> !events.contains(node);
  }

  @Override
  public Predicate<Node> checkLockInner() {
    Set<Node> allNodes = constraint.getAllNodes();
    return allNodes::contains;
  }

  public double adjustAndReturnError() {
    double expectedProb = constraint.getProbability();
    eventJointProb.reset();
    conditionJointProb.reset();
    complementJointProb.reset();

    VectorOdometer vectorOdometer = iterator.getController().getOdometer();
    double[] probs = vectorOdometer.getProbabilities();
    int[] stateIndexes = vectorOdometer.getStateIndexes();
    boolean[][] stateIsEvent = vectorOdometer.getNodeStateEvidenceArray();

    calculateProbability(probs, stateIndexes, stateIsEvent);

    double actualProb = getRatio(eventJointProb.sum(), conditionJointProb.sum());
    double complementProb = getRatio(complementJointProb.sum(), conditionJointProb.sum());

    double adjustmentRatio = getRatio(expectedProb, actualProb);
    double compRatio = getRatio((1 - expectedProb), complementProb);

    adjustToRatio(adjustmentRatio, compRatio, probs, stateIndexes, stateIsEvent);
    return storeError(Math.pow(actualProb - expectedProb, 2));
  }

  private void calculateProbability(double[] probs, int[] stateIndexes, boolean[][] stateIsEvent) {
    iterator.iterateOuter(
        () -> {
          boolean isEvidence = checkIsEvidence(stateIndexes, stateIsEvent);
          DoubleAdder correctAdder = isEvidence ? eventJointProb : complementJointProb;
          iterator.iterateInner(
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

  protected void adjustToRatio(
      double ratioIfEvent,
      double ratioOtherwise,
      double[] probs,
      int[] stateIndexes,
      boolean[][] stateIsEvent) {
    iterator.iterateOuter(
        () -> {
          double ratio =
              checkIsEvidence(stateIndexes, stateIsEvent) ? ratioIfEvent : ratioOtherwise;
          iterator.iterateInner((o, i) -> probs[i] = probs[i] * ratio);
        });
  }

  private double storeError(double error) {
    errors.add(error);
    return error;
  }

  protected boolean checkIsEvidence(int[] stateIndexes, boolean[][] stateIsEvent) {
    return IntStream.range(0, stateIsEvent.length)
        .filter(x -> stateIsEvent[x].length != 0)
        .allMatch(x -> stateIsEvent[x][stateIndexes[x]]);
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
}
