package io.github.alecredmond.export.application.constraints;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.Collection;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public abstract class ProbabilityConstraint {
  protected final Set<NodeState> eventStates;
  protected final Set<NodeState> conditionStates;
  protected final Set<NodeState> allStates;
  protected final double probability;
  protected final Set<Node> eventNodes;
  protected final Set<Node> conditionNodes;
  protected final Set<Node> allNodes;

  protected ProbabilityConstraint(
      @NonNull NodeState eventState, Collection<NodeState> conditionStates, double probability) {
    this.eventStates = Set.of(eventState);
    this.conditionStates = Set.copyOf(conditionStates);
    this.allStates = NodeUtils.combineStates(eventStates, conditionStates);
    this.probability = probability;
    this.eventNodes = NodeUtils.getNodes(eventStates);
    this.conditionNodes = NodeUtils.getNodes(conditionStates);
    this.allNodes = NodeUtils.getNodes(allStates);
  }
}
