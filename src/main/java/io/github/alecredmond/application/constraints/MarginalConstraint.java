package io.github.alecredmond.application.constraints;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.HashSet;
import lombok.Getter;

/** A constraint on the network {@code P(eventState == probability)} */
@Getter
public class MarginalConstraint extends ParameterConstraint {
  private final NodeState eventState;
  private final Node eventNode;

  public MarginalConstraint(NodeState eventState, double probability) {
    super(eventState, new HashSet<>(), probability);
    this.eventState = eventState;
    this.eventNode = eventState.getNode();
  }
}
