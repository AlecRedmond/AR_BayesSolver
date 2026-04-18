package io.github.alecredmond.internal.method.constraints.strategies;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;
import java.util.Set;

public interface ConstraintStrategy<T extends ProbabilityConstraint> {
  ConstraintSolverHandler<T> buildConstraintHandler(
      JTATableHandler tableHandler, ProbabilityConstraint constraint);

  ConstraintValidator<T> buildConstraintValidator();

  ConstraintSerializer<T> buildConstraintSerializer();

  ProbabilityConstraint buildConstraint(
      Set<NodeState> eventStates, Set<NodeState> conditionStates, double probability);
}
