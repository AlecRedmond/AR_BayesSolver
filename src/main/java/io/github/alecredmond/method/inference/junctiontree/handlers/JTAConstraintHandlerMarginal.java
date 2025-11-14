package io.github.alecredmond.method.inference.junctiontree.handlers;

import io.github.alecredmond.application.constraints.MarginalConstraint;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class JTAConstraintHandlerMarginal extends JTAConstraintHandler {

  public JTAConstraintHandlerMarginal(JTATableHandler jtaTableHandler, MarginalConstraint constraint) {
    super(jtaTableHandler, constraint);
  }

  @Override
  protected Set<Integer> getConditionIndexSet() {
    return Set.of();
  }

  @Override
  protected Set<Integer> getConstraintIndexeSet(Set<Integer> conditionIndexSet) {
    return jtaTableHandler.getIndexes(constraint.getAllStates());
  }

  @Override
  protected List<Integer> getComplementIndexList(
      Set<Integer> constraintIndexSet, Set<Integer> conditionIndexSet) {
    return IntStream.range(0, jtaTableHandler.getProbabilities().length)
        .filter(i -> !constraintIndexSet.contains(i))
        .boxed()
        .toList();
  }

  @Override
  protected double getConditionProb() {
    return 1.0;
  }
}
