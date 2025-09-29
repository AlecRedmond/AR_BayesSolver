package com.artools.method.indexer;

import com.artools.application.constraints.MarginalConstraint;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class MarginalIndexer extends ConstraintIndexer {

  public MarginalIndexer(TableIndexer tableIndexer, MarginalConstraint constraint) {
    super(tableIndexer, constraint);
  }

  @Override
  protected Set<Integer> getConditionIndexSet() {
    return Set.of();
  }

  @Override
  protected Set<Integer> getConstraintIndexeSet(Set<Integer> conditionIndexSet) {
    return tableIndexer.getIndexes(constraint.getAllStates());
  }

  @Override
  protected List<Integer> getComplementIndexList(
      Set<Integer> constraintIndexSet, Set<Integer> conditionIndexSet) {
    return IntStream.range(0, tableIndexer.getProbabilities().length)
        .filter(i -> !constraintIndexSet.contains(i))
        .boxed()
        .toList();
  }

  @Override
  protected double getConditionProb() {
    return 1.0;
  }
}
