package io.github.alecredmond.method.inference.junctiontree.handlers;

import io.github.alecredmond.application.constraints.ConditionalConstraint;
import io.github.alecredmond.application.node.NodeState;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JTAConstraintHandlerConditional extends JTAConstraintHandler {

  public JTAConstraintHandlerConditional(JTATableHandler jtaTableHandler, ConditionalConstraint constraint) {
    super(jtaTableHandler, constraint);
  }

  @Override
  protected Set<Integer> getConditionIndexSet() {
    return jtaTableHandler.getIndexes(constraint.getConditionStates());
  }

  @Override
  protected Set<Integer> getConstraintIndexeSet(Set<Integer> conditionIndexSet) {
    Set<Integer> conIndSet = new HashSet<>(conditionIndexSet);
    for (NodeState state : constraint.getEventStates()) {
      conIndSet.retainAll(jtaTableHandler.getIndexes(state));
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
    return jtaTableHandler.sumFromTableIndexes(conditionIndexes);
  }
}
