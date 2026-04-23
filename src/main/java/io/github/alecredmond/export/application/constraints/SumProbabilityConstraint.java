package io.github.alecredmond.export.application.constraints;

import io.github.alecredmond.export.application.node.NodeState;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SumProbabilityConstraint extends ProbabilityConstraint {
  public SumProbabilityConstraint(
      Collection<NodeState> eventStates,
      Collection<NodeState> conditionStates,
      double probability) {
    super(eventStates, conditionStates, probability);
  }
}
