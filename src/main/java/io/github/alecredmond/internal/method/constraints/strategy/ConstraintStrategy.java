package io.github.alecredmond.internal.method.constraints.strategy;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.base.ConstraintSolverBase;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.JunctionTreeTableQueryTool;

public interface ConstraintStrategy<T extends ProbabilityConstraint> {
  default ConstraintSolver buildSolverHandler(
      JunctionTreeTableQueryTool tableHelper, ProbabilityConstraint constraint) {
    return new ConstraintSolverBase(constraint, tableHelper.getTable());
  }

  ConstraintValidator<T, ?> getConstraintValidator();

  boolean constraintIsInstance(ProbabilityConstraint constraint);

  boolean serializedIsInstance(SerializedProbabilityConstraint serialized);

  ConstraintSerializer<T, ?> getConstraintSerializer();

  <P extends ProbabilityConstraint> T safeCastConstraint(P constraint);
}
