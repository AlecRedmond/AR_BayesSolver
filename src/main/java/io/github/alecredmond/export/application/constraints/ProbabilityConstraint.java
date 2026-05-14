package io.github.alecredmond.export.application.constraints;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.Collection;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A probability distribution constraint in the form {@code P(E|C) = p}. During the solving process,
 * the solver will attempt to find a solution minimizing the divergence from the constraints
 * assigned to it.
 *
 * <p>Constraints do not need to follow the network's graph ordering, {@code P(X|Pa(X))}. However,
 * constraints that span conditionally independent nodes cannot be losslessly projected back to
 * individual CPT entries, and may produce different inference results after fitting.
 *
 * @author Alec Redmond
 */
@Getter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode
public abstract class ProbabilityConstraint {
  @EqualsAndHashCode.Include @ToString.Include protected final Set<NodeState> eventStates;
  @EqualsAndHashCode.Include @ToString.Include protected final Set<NodeState> conditionStates;
  protected final Set<NodeState> allStates;
  @EqualsAndHashCode.Include @ToString.Include protected final double probability;
  protected final Set<Node> eventNodes;
  protected final Set<Node> conditionNodes;
  protected final Set<Node> allNodes;

  protected ProbabilityConstraint(
      Collection<NodeState> eventStates,
      Collection<NodeState> conditionStates,
      double probability) {
    this.eventStates = Set.copyOf(eventStates);
    this.conditionStates = Set.copyOf(conditionStates);
    this.allStates = NodeUtils.combineStates(eventStates, conditionStates);
    this.probability = probability;
    this.eventNodes = NodeUtils.getNodes(eventStates);
    this.conditionNodes = NodeUtils.getNodes(conditionStates);
    this.allNodes = NodeUtils.getNodes(allStates);
  }
}
