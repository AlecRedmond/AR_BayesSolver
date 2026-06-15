package io.github.alecredmond.export.application.network;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.application.probabilitytables.ObservedTable;
import io.github.alecredmond.export.method.inference.BayesSolver;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import java.io.Serializable;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Object containing the data which constitutes a {@link BayesianNetwork}.
 *
 * <p><b>WARNING: DO NOT MODIFY THIS DATA!</b> Use the methods given in {@link BayesianNetwork} to
 * modify the network's structure, add constraints, etc.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BayesianNetworkData {
  /** The name of the {@link BayesianNetwork} */
  private String networkName = "UNNAMED NETWORK";

  /**
   * The list of {@link Node}s present within the {@link BayesianNetwork}. When this data is built
   * using {@link BayesianNetwork#buildNetworkData()}, the nodes will be in topological order, with
   * root nodes at the start and leaf nodes at the end.
   */
  private List<Node> nodes = new ArrayList<>();

  /**
   * Returns a map from each {@link Node} id to its corresponding {@link Node} instance within the
   * {@link BayesianNetwork}.
   */
  private Map<Serializable, Node> nodeIDsMap = new HashMap<>();

  /**
   * Returns a map from each {@link NodeState} id to its corresponding {@link NodeState} instance
   * within the {@link BayesianNetwork}.
   */
  private Map<Serializable, NodeState> nodeStateIDsMap = new HashMap<>();

  /**
   * Returns a map from each {@link Node} to its corresponding {@link NetworkTable} (CPT) instance
   * within the {@link BayesianNetwork}. These are built when calling {@link
   * BayesianNetwork#buildNetworkData()}.
   */
  private Map<Node, NetworkTable> networkTablesMap = new LinkedHashMap<>();

  /** A list of all {@link ProbabilityConstraint}s on the {@link BayesianNetwork}. */
  private List<ProbabilityConstraint> constraints = new ArrayList<>();

  /**
   * {@code true} if a successful {@link BayesSolver} run has been carried out on the {@link
   * BayesianNetwork}, otherwise {@code false}.
   */
  private boolean solved = false;

  /**
   * Returns the network's conditional probability table (CPT) associated with the given Node's ID.
   * This be a {@link ObservedTable} if referencing a root node, or a {@link ConditionalTable}
   * otherwise.
   *
   * @param <T> the class of the Node's ID
   * @param nodeID an ID associated with a node in the network
   * @return the network CPT associated with the Node
   */
  public <T extends Serializable> NetworkTable getNetworkTableById(T nodeID) {
    return networkTablesMap.get(nodeIDsMap.get(nodeID));
  }
}
