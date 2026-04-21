package io.github.alecredmond.internal.method.constraints.types.conditionalconstraint;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolverHandler;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.BaseConstraintSolverHandler;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;

public class ConditionalConstraintSolverHandler extends BaseConstraintSolverHandler
    implements ConstraintSolverHandler<ConditionalConstraint> {

  public ConditionalConstraintSolverHandler(
      JTATableHandler jtaTableHandler,
      ConditionalConstraint constraint,
      VectorOdometer vectorOdometer) {
    super(jtaTableHandler, constraint, vectorOdometer);
  }
}
