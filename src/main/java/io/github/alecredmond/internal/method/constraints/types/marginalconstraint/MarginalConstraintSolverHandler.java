package io.github.alecredmond.internal.method.constraints.types.marginalconstraint;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolverHandler;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.BaseConstraintSolverHandler;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;

public class MarginalConstraintSolverHandler extends BaseConstraintSolverHandler
    implements ConstraintSolverHandler<MarginalConstraint> {

  public MarginalConstraintSolverHandler(
      JTATableHandler jtaTableHandler,
      MarginalConstraint constraint,
      VectorOdometer vectorOdometer) {
    super(jtaTableHandler, constraint, vectorOdometer);
  }
}
