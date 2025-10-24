package io.github.alecredmond.application.constraints;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

/**
 * Represents a <b>Marginal Probability Constraint</b> in a Bayesian Network. This is a special case of
 * a conditional constraint where the condition set is empty, typically representing P(EventState).
 */
@Getter
public class MarginalConstraint extends ParameterConstraint {
  private final NodeState eventState;
  private final Node eventNode;

  public MarginalConstraint(NodeState eventState, double probability) {
    super(Set.of(eventState), new HashSet<>(), probability);
    this.eventState = eventState;
    this.eventNode = eventState.getNode();
  }
}
