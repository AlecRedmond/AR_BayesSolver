package io.github.alecredmond.internal.method.constraints.types.combinedconstraint;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.constraints.SumProbabilityConstraint;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolverHandler;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.BaseConstraintSolver;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;

public class SumConstraintSolverHandler extends BaseConstraintSolver
    implements ConstraintSolverHandler<SumProbabilityConstraint> {
  protected SumConstraintSolverHandler(
      JTATableHandler tableHandler,
      ProbabilityConstraint constraint,
      VectorOdometer vectorOdometer) {
    super(tableHandler, constraint, vectorOdometer);
  }
}
