package io.github.alecredmond.export.application.constraints;

import io.github.alecredmond.export.application.node.NodeState;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Defines the joint probability of multiple concurrent events across different nodes, optionally
 * subject to conditioning states, in the form {@code P(E1,E2,..,En|C) = p}. Unlike {@link
 * SumProbabilityConstraint}, this represents the probability of the intersection of these states.
 * As such, each event state MUST be from a different Node; the probability of two states in the
 * same node being simultaneously true is zero.
 *
 * <p>Valid examples:
 *
 * <ul>
 *   <li>{@code P(RAIN:TRUE, SPRINKLER:TRUE) = 0.01}<br>
 *       Unconditional joint probability
 *   <li>{@code P(SPRINKLER:FALSE,WET_GRASS:TRUE|RAIN:TRUE) = 0.99}<br>
 *       Conditional joint probability
 * </ul>
 *
 * @see ProbabilityConstraint
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class JointProbabilityConstraint extends ProbabilityConstraint {
  public JointProbabilityConstraint(
      Collection<NodeState> eventStates,
      Collection<NodeState> conditionStates,
      double probability) {
    super(eventStates, conditionStates, probability);
  }
}
