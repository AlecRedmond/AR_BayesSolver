package io.github.alecredmond.internal.method.constraints.types.marginalconstraint;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;
import java.util.Set;

public class MarginalConstraintStrategy implements ConstraintStrategy<MarginalConstraint> {

  @Override
  public MarginalConstraintSolverHandler buildConstraintHandler(
      JTATableHandler tableHandler, ProbabilityConstraint constraint) {
    return new MarginalConstraintSolverHandler(tableHandler, (MarginalConstraint) constraint);
  }

  @Override
  public ConstraintValidator<MarginalConstraint> buildConstraintValidator() {
    return new MarginalConstraintValidator();
  }

  @Override
  public MarginalConstraintSerializer buildConstraintSerializer() {
    return new MarginalConstraintSerializer();
  }

  @Override
  public ProbabilityConstraint buildConstraint(
      Set<NodeState> eventStates, Set<NodeState> conditionStates, double probability) {
    return new MarginalConstraint(eventStates.stream().findFirst().orElseThrow(), probability);
  }
}
