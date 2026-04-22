package io.github.alecredmond.internal.method.constraints.types.conditionalconstraint;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.BaseConstraintSolverHandlerFactory;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;

public class ConditionalSolverHandlerFactory
    extends BaseConstraintSolverHandlerFactory<
        ConditionalConstraint, ConditionalConstraintSolverHandler> {
  public ConditionalSolverHandlerFactory(
      JTATableHandler tableHandler, ConditionalConstraint constraint) {
    super(tableHandler, constraint);
  }

  @Override
  protected ConditionalConstraintSolverHandler constructIterator() {
    return new ConditionalConstraintSolverHandler(tableHandler, constraint, vectorOdometer);
  }
}
