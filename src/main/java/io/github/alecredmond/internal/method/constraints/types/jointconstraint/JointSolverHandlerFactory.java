package io.github.alecredmond.internal.method.constraints.types.jointconstraint;

import io.github.alecredmond.export.application.constraints.JointProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.BaseConstraintSolverHandlerFactory;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;
import java.util.function.Supplier;

public class JointSolverHandlerFactory
    extends BaseConstraintSolverHandlerFactory<
        JointProbabilityConstraint, JointConstraintSolverHandler> {
  public JointSolverHandlerFactory(
      JTATableHandler tableHandler, JointProbabilityConstraint constraint) {
    super(tableHandler, constraint);
  }

  @Override
  protected Supplier<JointConstraintSolverHandler> supplyIterator() {
    return () -> new JointConstraintSolverHandler(tableHandler, constraint, vectorOdometer);
  }
}
