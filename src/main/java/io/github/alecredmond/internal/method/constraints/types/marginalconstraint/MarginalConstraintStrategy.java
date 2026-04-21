package io.github.alecredmond.internal.method.constraints.types.marginalconstraint;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;

public class MarginalConstraintStrategy implements ConstraintStrategy<MarginalConstraint> {

  @Override
  public MarginalConstraintSolverHandler buildSolverHandler(
      JTATableHandler tableHandler, ProbabilityConstraint constraint) {
    return new MarginalSolverHandlerFactory(tableHandler, (MarginalConstraint) constraint).build();
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
