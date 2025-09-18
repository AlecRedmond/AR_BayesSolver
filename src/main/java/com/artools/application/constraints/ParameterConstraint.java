package com.artools.application.constraints;

import com.artools.application.node.Node;
import com.artools.application.node.NodeState;
import com.artools.method.node.NodeUtils;
import java.util.*;
import lombok.Getter;

@Getter
public class ParameterConstraint {
  protected final Set<NodeState> eventStates;
  protected final Set<NodeState> conditionStates;
  protected final Set<NodeState> allStates;
  protected final double probability;
  protected final Set<Node> eventNodes;
  protected final Set<Node> conditionNodes;
  protected final Set<Node> allNodes;

  public ParameterConstraint(
      Collection<NodeState> eventStates,
      Collection<NodeState> conditionStates,
      double probability) {
    if (eventStates.isEmpty())
      throw new IllegalArgumentException("Constraint constructor found empty event states!");
    this.eventStates = Set.copyOf(eventStates);
    this.conditionStates = Set.copyOf(conditionStates);
    this.allStates = Set.copyOf(NodeUtils.combineStates(eventStates, conditionStates));
    this.probability = probability;
    this.eventNodes = Set.copyOf(NodeUtils.getNodes(eventStates));
    this.conditionNodes = Set.copyOf(NodeUtils.getNodes(conditionStates));
    this.allNodes = Set.copyOf(NodeUtils.getNodes(allStates));
  }
}
