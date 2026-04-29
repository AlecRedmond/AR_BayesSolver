package io.github.alecredmond.internal.method.constraints.strategies;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.JunctionTreeTableHelper;

public interface ConstraintStrategy<T extends ProbabilityConstraint> {
  default ConstraintSolver buildSolverHandler(JunctionTreeTableHelper tableHelper, T constraint) {
    return new ConstraintSolverBase(constraint, tableHelper.getTable());
  }

  ConstraintValidator<T> buildConstraintValidator();

  ConstraintSerializer<T> buildConstraintSerializer();
}
