package io.github.alecredmond.export.application.constraints;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * A Constraint holding only one non-conditional event {@code P(E) = p}. This represents the
 * unconditional probability of the event in the network. If defining the network in graph order
 * {@code P(X|Pa(X))}, this is the constraint used for the unconditional probabilities of root
 * nodes.
 *
 * <p>Valid examples:
 *
 * <ul>
 *   <li>{@code P(RAIN:TRUE) = 0.8}<br>
 *       Graph-Ordered: The unconditional probability for P(RAIN)
 *   <li>{@code P(SPRINKLER:TRUE) = 0.6}<br>
 *       Non-Graph-Ordered: A soft marginal constraint on P(SPRINKLER)
 * </ul>
 *
 * @see ProbabilityConstraint
 */
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
