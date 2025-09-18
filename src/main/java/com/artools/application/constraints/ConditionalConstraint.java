package com.artools.application.constraints;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import lombok.Getter;

import java.util.Collection;
import java.util.Set;

@Getter
public class ConditionalConstraint extends ParameterConstraint {
  private final NodeState eventState;
  private final Node eventNode;

  public ConditionalConstraint(
      NodeState eventState, Collection<NodeState> conditionStates, double probability) {
    super(Set.of(eventState), conditionStates, probability);
    this.eventState = eventState;
    this.eventNode = eventState.getParentNode();
  }
}
