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
import lombok.Data;

/**
 * Container for data used in a {@link BayesianNetwork}. This includes the {@link NetworkTable}s,
 * {@link ProbabilityConstraint}s and maps linking all declared identifiers to their {@link Node}
 * and {@link NodeState} objects.
 *
 * <p><b>WARNING: MODIFYING THIS DATA MAY CAUSE UNDEFINED BEHAVIOUR!</b> The methods given in {@link
 * BayesianNetwork} should be used to modify the network's structure, add constraints, etc.
 *
 * @see BayesianNetwork
 * @author Alec Redmond
 */
@Data
public class BayesianNetworkData {
  /**
   * The list of {@link Node}s present within the {@link BayesianNetwork}. When this data is built
   * using {@link BayesianNetwork#buildNetworkData()}, the nodes will be in topological order, with
   * root nodes at the start and leaf nodes at the end.
   */
  private final List<Node> nodes = new ArrayList<>();

  /**
   * Returns a map from each {@link Node} id to its corresponding {@link Node} instance within the
   * {@link BayesianNetwork}.
   */
  private final Map<Serializable, Node> nodeIDsMap = new HashMap<>();

  /**
   * Returns a map from each {@link NodeState} id to its corresponding {@link NodeState} instance
   * within the {@link BayesianNetwork}.
   */
  private final Map<Serializable, NodeState> nodeStateIDsMap = new HashMap<>();

  /**
   * Returns a map from each {@link Node} to its corresponding {@link NetworkTable} (CPT) instance
   * within the {@link BayesianNetwork}. These are built when calling {@link
   * BayesianNetwork#buildNetworkData()}.
   */
  private final Map<Node, NetworkTable> networkTablesMap = new LinkedHashMap<>();

  /**
   * A {@link LinkedHashSet} of all {@link ProbabilityConstraint}s on the {@link BayesianNetwork}.
   */
  private final Set<ProbabilityConstraint> constraints = new LinkedHashSet<>();

  /** The name of the {@link BayesianNetwork} */
  private String networkName = "UNNAMED NETWORK";

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
