package io.github.alecredmond.application.constraints;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.Collection;
import lombok.Getter;

/** A constraint on the network {@code P(eventState | conditionState(s) == probability)} */
@Getter
public class ConditionalConstraint extends ParameterConstraint {

  private final NodeState eventState;
  private final Node eventNode;

  public ConditionalConstraint(
      NodeState eventState, Collection<NodeState> conditionStates, double probability) {
    super(eventState, conditionStates, probability);
    this.eventState = eventState;
    this.eventNode = eventState.getNode();
  }
}
