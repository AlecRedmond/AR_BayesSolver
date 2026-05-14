package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;
import java.io.Serializable;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base for a probability table used in a Bayesian Network, Solver, or Inference Engine.
 * Probability Tables contain information concerning the event and condition nodes active in the
 * table, a {@link ProbabilityVector} object which maps the Cartesian Product of all states to a
 * probability array, and a {@link TableHelper} which can be used for querying the table.
 *
 * <p>Within the user-facing end of AR_BayesSolver there are two accessible implementations:
 *
 * <ul>
 *   <li>{@link MarginalTable} maps the CPTs of root nodes in the network, and also the observed
 *       marginal values from inference.
 *   <li>{@link ConditionalTable} maps the CPTs of non-root nodes in the network where {@code
 *       P(X|Pa(X))}.
 * </ul>
 *
 * @author Alec Redmond
 */
@Getter
@EqualsAndHashCode()
@Slf4j
public abstract class ProbabilityTable {
  protected final Map<Serializable, NodeState> nodeStateIDMap;
  protected final Map<Serializable, Node> nodeIDMap;
  protected final ProbabilityVector vector;
  protected final Set<Node> nodes;
  protected final Set<Node> events;
  protected final Set<Node> conditions;
  @EqualsAndHashCode.Exclude protected final TableHelper<?> helper;
  @EqualsAndHashCode.Exclude @Setter protected Serializable tableName;

  protected <T extends Serializable> ProbabilityTable(
      Map<Serializable, NodeState> nodeStateIDMap,
      Map<Serializable, Node> nodeIDMap,
      ProbabilityVector vector,
      T tableName,
      Set<Node> nodes,
      Set<Node> events,
      Set<Node> conditions) {
    this.nodeStateIDMap = nodeStateIDMap;
    this.nodeIDMap = nodeIDMap;
    this.vector = vector;
    this.tableName = tableName;
    this.nodes = nodes;
    this.events = events;
    this.conditions = conditions;
    this.helper = buildHelper();
  }

  @SuppressWarnings("rawtypes")
  protected abstract TableHelper buildHelper();

  @SuppressWarnings("rawtypes")
  public abstract TableHelper getHelper();
}
