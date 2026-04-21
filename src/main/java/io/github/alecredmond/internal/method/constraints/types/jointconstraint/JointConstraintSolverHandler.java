package io.github.alecredmond.internal.method.constraints.types.jointconstraint;

import io.github.alecredmond.export.application.constraints.JointProbabilityConstraint;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolverHandler;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.BaseConstraintSolverHandler;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;

public class JointConstraintSolverHandler extends BaseConstraintSolverHandler
    implements ConstraintSolverHandler<JointProbabilityConstraint> {

  public JointConstraintSolverHandler(
      JTATableHandler tableHandler,
      JointProbabilityConstraint constraint,
      VectorOdometer vectorOdometer) {
    super(tableHandler, constraint, vectorOdometer);
  }
}
