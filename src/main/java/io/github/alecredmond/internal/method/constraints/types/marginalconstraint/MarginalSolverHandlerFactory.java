package io.github.alecredmond.internal.method.constraints.types.marginalconstraint;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.BaseConstraintSolverHandlerFactory;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;

public class MarginalSolverHandlerFactory
    extends BaseConstraintSolverHandlerFactory<MarginalConstraint> {

  protected MarginalSolverHandlerFactory(
      JTATableHandler tableHandler, MarginalConstraint constraint) {
    super(tableHandler, constraint);
  }

  @Override
  public MarginalConstraintSolverHandler build() {
    return superBuild(MarginalConstraintSolverHandler.class);
  }

  @Override
  protected VectorIterator constructIterator() {
    return new MarginalConstraintSolverHandler(tableHandler, constraint, vectorOdometer);
  }
}
