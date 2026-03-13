package io.github.alecredmond.export.application.constraints;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.util.HashSet;
import lombok.Getter;

@Getter
public class MarginalConstraint extends ProbabilityConstraint {
  private final NodeState eventState;
  private final Node eventNode;

  public MarginalConstraint(NodeState eventState, double probability) {
    super(eventState, new HashSet<>(), probability);
    this.eventState = eventState;
    this.eventNode = eventState.getNode();
  }
}
