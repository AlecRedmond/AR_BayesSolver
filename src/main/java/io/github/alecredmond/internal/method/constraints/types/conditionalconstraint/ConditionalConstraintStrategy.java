package io.github.alecredmond.internal.method.constraints.types.conditionalconstraint;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSerializer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;

public class ConditionalConstraintStrategy implements ConstraintStrategy<ConditionalConstraint> {
  @Override
  public ConditionalConstraintSolverHandler buildSolverHandler(
      JTATableHandler tableHandler, ProbabilityConstraint constraint) {
      return new ConditionalSolverHandlerFactory(tableHandler, (ConditionalConstraint) constraint).build();
  }

  @Override
  public ConditionalConstraintValidator buildConstraintValidator() {
    return new ConditionalConstraintValidator();
  }

  @Override
  public ConstraintSerializer<ConditionalConstraint> buildConstraintSerializer() {
    return new ConditionalConstraintSerializer();
  }

}
