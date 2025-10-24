package io.github.alecredmond.application.constraints;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.Collection;
import java.util.Set;
import lombok.Getter;

/**
 * Represents a <b>Conditional Probability Constraint</b> in a Bayesian Network. This constraint is
 * defined by a single event state and one or more condition states, typically representing
 * P(EventState | ConditionStates).
 */
@Getter
public class ConditionalConstraint extends ParameterConstraint {
  /** The single state defining the event part of the conditional constraint. */
  private final NodeState eventState;

  /** The node to which the {@code eventState} belongs. */
  private final Node eventNode;

  /**
   * Constructs a new Conditional Probability Constraint.
   *
   * @param eventState The single {@link NodeState} that is the event (e.g., A in P(A|B)).
   * @param conditionStates A collection of {@link NodeState} objects that are the condition (e.g.,
   *     B in P(A|B)).
   * @param probability The probability value for the constraint.
   */
  public ConditionalConstraint(
      NodeState eventState, Collection<NodeState> conditionStates, double probability) {
    super(Set.of(eventState), conditionStates, probability);
    this.eventState = eventState;
    this.eventNode = eventState.getNode();
  }
}
