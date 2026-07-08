package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.application.probabilitytables.cptentry.CptEntry;
import io.github.alecredmond.export.application.probabilitytables.cptentry.CptRow;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * A helper attached to a {@link NetworkTable} which provides additional methods for the table. This
 * provides all the methods contained within {@link TableQueryTool}, plus additional methods to
 * query the conditional probability of the CPTs within the network.
 *
 * @see ConditionalTableQueryTool
 * @see RootNodeTableQueryTool
 * @author Alec Redmond
 */
public interface NetworkTableQueryTool extends TableQueryTool {
  /**
   * Returns a cross-section of probabilities for every {@link NodeState} in the Network {@link
   * Node}, under the given conditions.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, the input must
   * contain a {@link NodeState} for every {@link Node} value in {@link
   * NetworkTable#getConditions()}, otherwise the method will return an empty {@link HashMap}.
   *
   * @param condition a collection of {@link NodeState} values which define a condition within the
   *     table
   * @return a new map of each Network node's {@link NodeState} and its probability under the given
   *     conditions, or an empty {@link HashMap} if Safe Mode validation failed.
   */
  Map<NodeState, Double> getConditionalProb(Collection<NodeState> condition);

  /**
   * Returns a cross-section of probabilities for every {@link NodeState} in the Network {@link
   * Node}, under the given conditions.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, the input must
   * contain a {@link NodeState} for every {@link Node} value in {@link
   * NetworkTable#getConditions()}, otherwise the method will return an empty {@link HashMap}.
   *
   * @param <S> the class of the {@link NodeState} identifiers.
   * @param conditionIDs a collection of identifiers, each linked to a {@link NodeState} value which
   *     defines a condition within the table
   * @return a new map of each Network node's {@link NodeState} and its probability under the given
   *     conditions, or an empty {@link HashMap} if Safe Mode validation failed.
   */
  <S extends Serializable> Map<NodeState, Double> getConditionalProbByIds(
      Collection<S> conditionIDs);

  /**
   * Builds a copy of the {@link NetworkTable} this handler is connected to. This deep-copies
   * everything except the {@link Node} and {@link NodeState} values, which maintain the original
   * references.
   *
   * @return a copy of the current table.
   */
  NetworkTable copyTable();

  /**
   * Consumes an operation for every condition state combination within this {@code NetworkTable}.
   * Every {@link CptRow} contains a {@link CptEntry} list of the exact size and order as the {@link
   * NodeState} ordering in this table's network {@link Node}.
   *
   * @param conditionalRowConsumer an operation to be completed for each perturbation of condition
   *     states.
   */
  void iterateOverConditions(Consumer<CptRow> conditionalRowConsumer);

  /**
   * Returns every {@link CptEntry} within this {@code NetworkTable}. A {@link CptEntry} contains
   * details of the measured {@link NodeState} within the table's Network {@link Node}, any
   * conditioning states acting upon it, its conditional probability, and a reference to its index
   * in the probability {@code double[]} in this {@code NetworkTable}.
   *
   * @return A new list of {@link CptEntry} records, ordered identically to this {@code
   *     NetworkTable}'s probability array.
   */
  List<CptEntry> getCptEntries();
}
