package com.artools.method.indexer;

import com.artools.application.constraints.ConditionalConstraint;
import com.artools.application.node.NodeState;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConditionalIndexer extends ConstraintIndexer {

  public ConditionalIndexer(TableIndexer tableIndexer, ConditionalConstraint constraint) {
    super(tableIndexer, constraint);
  }

  @Override
  protected Set<Integer> getConditionIndexSet() {
    return tableIndexer.getIndexes(constraint.getConditionStates());
  }

  @Override
  protected Set<Integer> getConstraintIndexeSet(Set<Integer> conditionIndexSet) {
    Set<Integer> cis = new HashSet<>(conditionIndexSet);
    for (NodeState state : constraint.getEventStates()) {
      cis.retainAll(tableIndexer.getIndexes(state));
    }
    return cis;
  }

  @Override
  protected List<Integer> getComplementIndexList(
      Set<Integer> constraintIndexSet, Set<Integer> conditionIndexSet) {
    Set<Integer> comps = new HashSet<>(conditionIndexSet);
    comps.removeAll(constraintIndexSet);
    return comps.stream().toList();
  }

    @Override
    protected double getConditionProb() {
        return tableIndexer.sumFromTableIndexes(conditionIndexes);
    }


}
