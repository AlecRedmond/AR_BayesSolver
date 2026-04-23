package io.github.alecredmond.internal.method.constraints.types.jointconstraint;

import io.github.alecredmond.export.application.constraints.JointProbabilityConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolverHandler;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.BaseConstraintSolver;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;

public class JointConstraintSolverHandler extends BaseConstraintSolver
    implements ConstraintSolverHandler<JointProbabilityConstraint> {

  public JointConstraintSolverHandler(
      JTATableHandler tableHandler,
      ProbabilityConstraint constraint,
      VectorOdometer vectorOdometer) {
    super(tableHandler, constraint, vectorOdometer);
  }
}
