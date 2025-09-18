package com.artools.application.constraints;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class MarginalConstraint extends ParameterConstraint {
  private final NodeState eventState;
  private final Node eventNode;

  public MarginalConstraint(NodeState eventState, double probability) {
    super(Set.of(eventState), new HashSet<>(), probability);
    this.eventState = eventState;
    this.eventNode = eventState.getParentNode();
  }
}
