package io.github.alecredmond.export.application.constraints;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class ConditionalConstraint extends ProbabilityConstraint {
  private final NodeState eventState;
  private final Node eventNode;

  public ConditionalConstraint(
      NodeState eventState, Collection<NodeState> conditionStates, double probability) {
    super(eventState, conditionStates, probability);
    this.eventState = eventState;
    this.eventNode = eventState.getNode();
  }
}
