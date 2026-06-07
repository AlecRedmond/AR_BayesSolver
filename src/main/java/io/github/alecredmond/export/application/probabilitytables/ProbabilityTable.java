package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Interface for a probability table used in a Bayesian Network, Solver, or Inference Engine.
 * Probability Tables contain information concerning the event and condition nodes active in the
 * table, a {@link ProbabilityVector} object which maps the Cartesian Product of all states to a
 * probability array, and a {@link TableHelper} which can be used for querying the table.
 *
 * @see NetworkTable
 * @see ObservedTable
 * @author Alec Redmond
 */
public interface ProbabilityTable {
  Map<Serializable, NodeState> getNodeStateIDMap();

  Map<Serializable, Node> getNodeIDMap();

  ProbabilityVector getVector();

  Set<Node> getNodes();

  Set<Node> getEvents();

  Set<Node> getConditions();

  Serializable getTableName();

  @SuppressWarnings("rawtypes")
  TableHelper getHelper();

  double[] getProbabilities();
}
