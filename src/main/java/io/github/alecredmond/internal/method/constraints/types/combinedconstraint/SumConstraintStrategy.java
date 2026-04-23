package io.github.alecredmond.internal.method.constraints.types.combinedconstraint;

import io.github.alecredmond.export.application.constraints.SumProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSerializer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolverHandlerFactory;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.strategies.baseobjects.ConstraintValidator;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;

public class SumConstraintStrategy implements ConstraintStrategy<SumProbabilityConstraint> {

  @Override
  public SumConstraintSolverHandler buildSolverHandler(
      JTATableHandler tableHandler, SumProbabilityConstraint constraint) {
    return new ConstraintSolverHandlerFactory<>(
            tableHandler, constraint, SumConstraintSolverHandler::new)
        .build();
  }

  @Override
  public ConstraintValidator<SumProbabilityConstraint> buildConstraintValidator() {
    return new SumConstraintValidator();
  }

  @Override
  public ConstraintSerializer<SumProbabilityConstraint> buildConstraintSerializer() {
    return new SumConstraintSerializer();
  }
}
