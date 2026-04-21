package io.github.alecredmond.internal.method.constraints.types.jointconstraint;

import io.github.alecredmond.export.application.constraints.JointProbabilityConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSerializer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolverHandler;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;

public class JointConstraintStrategy implements ConstraintStrategy<JointProbabilityConstraint> {

  @Override
  public ConstraintSolverHandler<JointProbabilityConstraint> buildSolverHandler(
      JTATableHandler tableHandler, ProbabilityConstraint constraint) {
    return new JointSolverHandlerFactory(tableHandler, (JointProbabilityConstraint) constraint).build();
  }

  @Override
  public ConstraintValidator<JointProbabilityConstraint> buildConstraintValidator() {
    return new JointConstraintValidator();
  }

  @Override
  public ConstraintSerializer<JointProbabilityConstraint> buildConstraintSerializer() {
    return new JointConstraintSerializer();
  }

}
