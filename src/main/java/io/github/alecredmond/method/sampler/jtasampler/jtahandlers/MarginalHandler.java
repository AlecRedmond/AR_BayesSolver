package io.github.alecredmond.method.sampler.jtasampler.jtahandlers;

import io.github.alecredmond.application.constraints.MarginalConstraint;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class MarginalHandler extends ConstraintHandler {

  public MarginalHandler(JunctionTableHandler junctionTableHandler, MarginalConstraint constraint) {
    super(junctionTableHandler, constraint);
  }

  @Override
  protected Set<Integer> getConditionIndexSet() {
    return Set.of();
  }

  @Override
  protected Set<Integer> getConstraintIndexeSet(Set<Integer> conditionIndexSet) {
    return junctionTableHandler.getIndexes(constraint.getAllStates());
  }

  @Override
  protected List<Integer> getComplementIndexList(
      Set<Integer> constraintIndexSet, Set<Integer> conditionIndexSet) {
    return IntStream.range(0, junctionTableHandler.getProbabilities().length)
        .filter(i -> !constraintIndexSet.contains(i))
        .boxed()
        .toList();
  }

  @Override
  protected double getConditionProb() {
    return 1.0;
  }
}
