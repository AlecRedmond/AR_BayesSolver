package io.github.alecredmond.internal.method.constraints.base;

import static io.github.alecredmond.internal.method.utils.DoublePrecision.*;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.inference.junctiontree.Clique;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSolver;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.PermanentLocksResetLogic;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.ResetLogicUtils;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.updatelogictypes.BlankUpdater;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConstraintSolverBase
    implements PermanentLocksResetLogic, BlankUpdater, ConstraintSolver {
  protected final VectorIterator<VectorOdometer> iterator;
  protected final ProbabilityConstraint constraint;
  protected final double[] eventJointProb = {0.0};
  protected final double[] conditionJointProb = {0.0};
  protected final double[] complementJointProb = {0.0};
  protected final List<Double> errors = new ArrayList<>();
  protected final boolean[] isEvidence;
  protected final int[] isEvidenceIndex = {0};

  public ConstraintSolverBase(ProbabilityConstraint constraint, JunctionTreeTable table) {
    this.constraint = constraint;
    this.iterator = new VectorIterator<>(table.getVector(), this, VectorOdometer::new);
    this.isEvidence = ResetLogicUtils.buildIsEvidenceIndex(iterator);
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
    resetAccumulators();
    VectorOdometer vectorOdometer = iterator.getController().getOdometer();
    double[] probs = vectorOdometer.getProbabilities();
    calculateProbability(probs);

    double expectedProb = constraint.getProbability();
    double actualProb = getRatio(eventJointProb[0], conditionJointProb[0]);

    if (!fuzzyEquals(actualProb, expectedProb)) {
      double complementProb = getRatio(complementJointProb[0], conditionJointProb[0]);
      double adjustmentRatio = getRatio(expectedProb, actualProb);
      double compRatio = getRatio((1 - expectedProb), complementProb);
      adjustToRatio(adjustmentRatio, compRatio, probs);
    }

    return storeError(Math.pow(actualProb - expectedProb, 2));
  }

  public void updateResults(
      Map<ProbabilityConstraint, double[]> results, int lastCycle, Set<Clique> cliques) {
    int runsPerCycle = cliques.size();
    double[] errorArray = new double[lastCycle + 1];
    int cycle = 0;
    int run = 0;
    for (double error : errors) {
      errorArray[cycle] += error;
      run++;
      if (run != runsPerCycle) continue;
      run = 0;
      cycle++;
    }
    if (constraintInMapWithHigherError(results, constraint, errorArray)) return;
    results.put(constraint, errorArray);
  }

  private boolean constraintInMapWithHigherError(
      Map<ProbabilityConstraint, double[]> results,
      ProbabilityConstraint constraint,
      double[] errorArray) {
    if (!results.containsKey(constraint)) return false;
    double previousError = Arrays.stream(results.get(constraint)).sum();
    double currentError = Arrays.stream(errorArray).sum();
    return previousError > currentError;
  }

  private void resetAccumulators() {
    eventJointProb[0] = 0.0;
    conditionJointProb[0] = 0.0;
    complementJointProb[0] = 0.0;
  }

  private void calculateProbability(double[] probs) {
    isEvidenceIndex[0] = 0;
    iterator.iterateOuter(
        () -> {
          boolean e = isEvidence[isEvidenceIndex[0]];
          isEvidenceIndex[0] += 1;
          double[] correctAdder = e ? eventJointProb : complementJointProb;
          iterator.iterateInner(
              (o, i) -> {
                double prob = probs[i];
                conditionJointProb[0] += prob;
                correctAdder[0] += prob;
              });
        });
  }

  protected double getRatio(double targetProb, double actualProb) {
    return actualProb == 0 ? 0.0 : targetProb / actualProb;
  }

  protected void adjustToRatio(double ratioIfEvent, double ratioOtherwise, double[] probs) {
    isEvidenceIndex[0] = 0;
    iterator.iterateOuter(
        () -> {
          boolean e = isEvidence[isEvidenceIndex[0]];
          isEvidenceIndex[0] += 1;
          double ratio = e ? ratioIfEvent : ratioOtherwise;
          iterator.iterateInner((o, i) -> probs[i] = probs[i] * ratio);
        });
  }

  private double storeError(double error) {
    errors.add(error);
    return error;
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
    return ResetLogicUtils.updateEvidenceArrayFunction(eventNodes, eventStates);
  }
}
