package com.artools.method.solver.netsampler;

import com.artools.application.constraints.ParameterConstraint;
import com.artools.application.constraints.SumToOneConstraint;
import com.artools.application.node.NodeState;
import com.artools.application.probabilitytables.JunctionTreeTable;
import com.artools.method.probabilitytables.TableUtils;
import java.util.Set;
import lombok.Getter;

@Getter
public class IterativeProportionalFitter {
  private double error;

  public IterativeProportionalFitter() {}

  public IterativeProportionalFitter fitData(
      ParameterConstraint constraint, JunctionTreeTable table) {
    double conditionalMass = getConditionalMass(constraint, table);
    double targetProb = constraint.getProbability();
    double actualProb = getActualProb(constraint, table, conditionalMass);
    double ratio = getRatio(targetProb, actualProb);

    if (ratio == 1) {
      error = 0;
      return this;
    }

    double complementRatio = getRatio((1 - targetProb), (1 - actualProb));

    table.getKeySet().stream()
        .filter(key -> filterKeySet(key, constraint))
        .forEach(
            key -> {
              double correctRatio = chooseRatio(constraint, key) ? ratio : complementRatio;
              table.setProbabilityByRatio(key, correctRatio);
            });

    error = Math.pow(targetProb - actualProb, 2);
    return this;
  }

  private double getConditionalMass(ParameterConstraint constraint, JunctionTreeTable table) {
    return constraint.getConditionNodes().isEmpty()
        ? 1.0
        : TableUtils.sumOfJointKey(table, constraint.getConditionStates());
  }

  private double getActualProb(
      ParameterConstraint constraint, JunctionTreeTable table, double conditionalMass) {
    return TableUtils.sumOfJointKey(table, constraint.getAllStates()) / conditionalMass;
  }

  private static double getRatio(double targetProb, double actualProb) {
    return actualProb == 0 ? 0.0 : targetProb / actualProb;
  }

  private boolean filterKeySet(Set<NodeState> key, ParameterConstraint constraint) {
    if (constraint.getConditionStates().isEmpty()) return true;
    return key.containsAll(constraint.getConditionStates());
  }

  private boolean chooseRatio(ParameterConstraint constraint, Set<NodeState> key) {
    return constraint instanceof SumToOneConstraint || key.containsAll(constraint.getEventStates());
  }
}
