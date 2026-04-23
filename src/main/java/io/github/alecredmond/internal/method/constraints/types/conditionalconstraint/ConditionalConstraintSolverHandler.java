package io.github.alecredmond.internal.method.constraints.types.conditionalconstraint;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolverHandler;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.BaseConstraintSolver;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;

public class ConditionalConstraintSolverHandler extends BaseConstraintSolver
    implements ConstraintSolverHandler<ConditionalConstraint> {

  public ConditionalConstraintSolverHandler(
      JTATableHandler jtaTableHandler,
      ProbabilityConstraint constraint,
      VectorOdometer vectorOdometer) {
    super(jtaTableHandler, constraint, vectorOdometer);
  }
}
