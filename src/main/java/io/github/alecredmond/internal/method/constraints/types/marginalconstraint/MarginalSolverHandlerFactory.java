package io.github.alecredmond.internal.method.constraints.types.marginalconstraint;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.BaseConstraintSolverHandlerFactory;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;
import java.util.function.Supplier;

public class MarginalSolverHandlerFactory
    extends BaseConstraintSolverHandlerFactory<
        MarginalConstraint, MarginalConstraintSolverHandler> {

  protected MarginalSolverHandlerFactory(
      JTATableHandler tableHandler, MarginalConstraint constraint) {
    super(tableHandler, constraint);
  }

  @Override
  protected Supplier<MarginalConstraintSolverHandler> supplyIterator() {
    return () -> new MarginalConstraintSolverHandler(tableHandler, constraint, vectorOdometer);
  }
}
