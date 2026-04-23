package io.github.alecredmond.internal.method.constraints.types.marginalconstraint;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolverHandlerFactory;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.ConstraintValidator;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;

public class MarginalConstraintStrategy implements ConstraintStrategy<MarginalConstraint> {

  @Override
  public MarginalConstraintSolverHandler buildSolverHandler(
      JTATableHandler tableHandler, MarginalConstraint constraint) {
    return new ConstraintSolverHandlerFactory<>(
            tableHandler, constraint, MarginalConstraintSolverHandler::new)
        .build();
  }

  @Override
  public ConstraintValidator<MarginalConstraint> buildConstraintValidator() {
    return new MarginalConstraintValidator();
  }

  @Override
  public MarginalConstraintSerializer buildConstraintSerializer() {
    return new MarginalConstraintSerializer();
  }
}
