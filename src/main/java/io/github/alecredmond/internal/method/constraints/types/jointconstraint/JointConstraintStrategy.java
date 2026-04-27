package io.github.alecredmond.internal.method.constraints.types.jointconstraint;

import io.github.alecredmond.export.application.constraints.JointProbabilityConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSerializer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolverHandler;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintValidator;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;
import java.util.Set;

public class JointConstraintStrategy implements ConstraintStrategy<JointProbabilityConstraint> {

  @Override
  public ConstraintSolverHandler<JointProbabilityConstraint> buildConstraintHandler(
      JTATableHandler tableHandler, ProbabilityConstraint constraint) {
    return new JointConstraintSolverHandler(tableHandler, (JointProbabilityConstraint) constraint);
  }

  @Override
  public ConstraintValidator<JointProbabilityConstraint> buildConstraintValidator() {
    return new JointConstraintValidator();
  }

  @Override
  public ConstraintSerializer<JointProbabilityConstraint> buildConstraintSerializer() {
    return new JointConstraintSerializer();
  }

  @Override
  public JointProbabilityConstraint buildConstraint(
      Set<NodeState> eventStates, Set<NodeState> conditionStates, double probability) {
    return new JointProbabilityConstraint(eventStates, conditionStates, probability);
  }
}
