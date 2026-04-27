package io.github.alecredmond.export.application.constraints;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
@Getter
public class MarginalConstraint extends ProbabilityConstraint {
  private final NodeState eventState;
  private final Node eventNode;

  public MarginalConstraint(@NonNull NodeState eventState, double probability) {
    super(Set.of(eventState), new HashSet<>(), probability);
    this.eventState = eventState;
    this.eventNode = eventState.getNode();
  }
}
