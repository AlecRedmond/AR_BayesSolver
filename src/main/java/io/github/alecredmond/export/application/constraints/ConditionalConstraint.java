package io.github.alecredmond.export.application.constraints;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.util.Collection;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * A constraint defining the conditional probability of a single node state given a set of
 * conditioning states, in the form {@code P(E|C) = p}.
 *
 * <p>If the conditions represent the exact set of a node's parents in the graph {@code P(X|Pa(X))},
 * this maps directly to an entry in to the node's CPT. Otherwise, the constraint represents a
 * non-graph-ordered conditional that will be projected back to the CPTs after solving.
 *
 * <p>Valid examples:
 *
 * <ul>
 *   <li>{@code P(SPRINKLER:TRUE|RAIN:TRUE) = 0.01}<br>
 *       Graph-Ordered: A valid CPT entry for P(SPRINKLER|RAIN)
 *   <li>{@code P(RAIN:TRUE|SPRINKLER:TRUE) = 0.01)}<br>
 *       Non-Graph-Ordered: A constraint on P(RAIN), conditional on its child P(SPRINKLER)
 * </ul>
 *
 * @see ProbabilityConstraint
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class ConditionalConstraint extends ProbabilityConstraint {
  private final NodeState eventState;
  private final Node eventNode;

  public ConditionalConstraint(
      @NonNull NodeState eventState, Collection<NodeState> conditionStates, double probability) {
    super(Set.of(eventState), conditionStates, probability);
    this.eventState = eventState;
    this.eventNode = eventState.getNode();
  }
}
