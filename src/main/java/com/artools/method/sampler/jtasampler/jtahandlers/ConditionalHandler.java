package com.artools.method.sampler.jtasampler.jtahandlers;

import com.artools.application.constraints.ConditionalConstraint;
import com.artools.application.node.NodeState;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConditionalHandler extends ConstraintHandler {

  public ConditionalHandler(JunctionTableHandler junctionTableHandler, ConditionalConstraint constraint) {
    super(junctionTableHandler, constraint);
  }

  @Override
  protected Set<Integer> getConditionIndexSet() {
    return junctionTableHandler.getIndexes(constraint.getConditionStates());
  }

  @Override
  protected Set<Integer> getConstraintIndexeSet(Set<Integer> conditionIndexSet) {
    Set<Integer> conIndSet = new HashSet<>(conditionIndexSet);
    for (NodeState state : constraint.getEventStates()) {
      conIndSet.retainAll(junctionTableHandler.getIndexes(state));
    }
    return conIndSet;
  }

  @Override
  protected List<Integer> getComplementIndexList(
      Set<Integer> constraintIndexSet, Set<Integer> conditionIndexSet) {
    Set<Integer> comIndSet = new HashSet<>(conditionIndexSet);
    comIndSet.removeAll(constraintIndexSet);
    return comIndSet.stream().toList();
  }

  @Override
  protected double getConditionProb() {
    return junctionTableHandler.sumFromTableIndexes(conditionIndexes);
  }
}
