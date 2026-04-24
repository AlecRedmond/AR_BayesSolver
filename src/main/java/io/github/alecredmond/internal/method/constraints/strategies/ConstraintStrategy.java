package io.github.alecredmond.internal.method.constraints.strategies;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.ConstraintValidator;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;

public interface ConstraintStrategy<T extends ProbabilityConstraint> {
  default ConstraintSolver buildSolverHandler(JTATableHandler tableHandler, T constraint) {
    return new ConstraintSolver(constraint, tableHandler.getTable());
  }

  ConstraintValidator<T> buildConstraintValidator();

  ConstraintSerializer<T> buildConstraintSerializer();
}
