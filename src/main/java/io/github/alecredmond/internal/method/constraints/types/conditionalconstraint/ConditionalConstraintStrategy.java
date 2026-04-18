package io.github.alecredmond.internal.method.constraints.types.conditionalconstraint;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSerializer;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintStrategy;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;
import java.util.Set;

public class ConditionalConstraintStrategy implements ConstraintStrategy<ConditionalConstraint> {
  @Override
  public ConditionalConstraintSolverHandler buildConstraintHandler(
      JTATableHandler tableHandler, ProbabilityConstraint constraint) {
    return new ConditionalConstraintSolverHandler(tableHandler, (ConditionalConstraint) constraint);
  }

  @Override
  public ConditionalConstraintValidator buildConstraintValidator() {
    return new ConditionalConstraintValidator();
  }

  @Override
  public ConstraintSerializer<ConditionalConstraint> buildConstraintSerializer() {
    return new ConditionalConstraintSerializer();
  }

  @Override
  public ProbabilityConstraint buildConstraint(
      Set<NodeState> events, Set<NodeState> conditions, double probability) {
    NodeState event = events.stream().findFirst().orElseThrow();
    return new ConditionalConstraint(event, conditions, probability);
  }
}
