package io.github.alecredmond.application.constraints;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.method.node.NodeUtils;
import java.util.Collection;
import java.util.Set;
import lombok.Getter;

/**
 * Represents a probabilistic constraint imposed on a Bayesian Network, typically in the form of a
 * probability P(EventStates | ConditionStates).<br>
 * This class serves as the base for more specific constraints like {@link ConditionalConstraint}
 * and {@link MarginalConstraint}.
 */
@Getter
public class ParameterConstraint {
  /** The set of states defining the event part of the constraint. */
  protected final Set<NodeState> eventStates;

  /** The set of states defining the condition part of the constraint. */
  protected final Set<NodeState> conditionStates;

  /** The union of eventStates and conditionStates, representing all states involved. */
  protected final Set<NodeState> allStates;

  /** The probability value associated with the constraint, P(events|conditions). */
  protected final double probability;

  /** The set of nodes associated with the eventStates. */
  protected final Set<Node> eventNodes;

  /** The set of nodes associated with the conditionStates. */
  protected final Set<Node> conditionNodes;

  /** The set of all nodes involved in the constraint (union of eventNodes and conditionNodes). */
  protected final Set<Node> allNodes;

  /**
   * Constructs a new probabilistic constraint.
   *
   * @param eventStates A collection of {@link NodeState} objects defining the event. Must not be
   *     empty.
   * @param conditionStates A collection of {@link NodeState} objects defining the condition.
   * @param probability The probability value for the constraint (e.g., P(A|B) = p).
   * @throws IllegalArgumentException if {@code eventStates} is empty.
   */
  public ParameterConstraint(
      Collection<NodeState> eventStates,
      Collection<NodeState> conditionStates,
      double probability) {
    if (eventStates.isEmpty())
      throw new IllegalArgumentException("Constraint constructor found empty event states!");
    this.eventStates = Set.copyOf(eventStates);
    this.conditionStates = Set.copyOf(conditionStates);
    this.allStates = NodeUtils.combineStates(eventStates, conditionStates);
    this.probability = probability;
    this.eventNodes = NodeUtils.getNodes(eventStates);
    this.conditionNodes = NodeUtils.getNodes(conditionStates);
    this.allNodes = NodeUtils.getNodes(allStates);
  }
}
