package io.github.alecredmond.internal.method.constraints.strategies;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.ConstraintValidator;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;

public interface ConstraintStrategy<T extends ProbabilityConstraint> {
  ConstraintSolverHandler<T> buildSolverHandler(
      JTATableHandler tableHandler, T constraint);

  ConstraintValidator<T> buildConstraintValidator();

  ConstraintSerializer<T> buildConstraintSerializer();

}
