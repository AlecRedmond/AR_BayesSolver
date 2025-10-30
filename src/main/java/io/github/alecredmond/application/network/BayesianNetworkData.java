package io.github.alecredmond.application.network;

import io.github.alecredmond.application.constraints.ParameterConstraint;
import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.MarginalTable;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents the data structure for a Bayesian Network. <br>
 * This class encapsulates the network's structure, including nodes, states, conditional probability
 * tables, observation data (evidence), constraints, and solver-related information.
 */
@Data
@AllArgsConstructor
public class BayesianNetworkData {
  /** The name identifier for the Bayesian Network. */
  protected String networkName;

  /** A list of all {@link Node} objects in the network. */
  protected List<Node> nodes;

  /** A map to retrieve a {@link Node} by its unique ID. */
  protected Map<Object, Node> nodeIDsMap;

  /** A map to retrieve a {@link NodeState} by its unique ID. */
  protected Map<Object, NodeState> nodeStateIDsMap;

  /** A map linking each {@link Node} to its primary {@link ProbabilityTable}. */
  protected Map<Node, ProbabilityTable> networkTablesMap;

  /** A map to store the observed or calculated marginal probabilities for each {@link Node}. */
  protected Map<Node, MarginalTable> observationMap;

  /**
   * A map storing the specific {@link NodeState} that has been observed as evidence for a given
   * {@link Node}.
   */
  protected Map<Node, NodeState> observed;

  /** A list of all probability-related constraints applied to the network. */
  protected List<ParameterConstraint> constraints;

  /** A flag indicating whether the network has been solved by an inference algorithm. */
  protected boolean solved;

  /**
   * Default constructor that initializes all internal collections and sets default values. All
   * lists and maps are initialized as empty.
   */
  public BayesianNetworkData() {
    this.networkName = "";
    this.nodes = new ArrayList<>();
    this.nodeIDsMap = new HashMap<>();
    this.nodeStateIDsMap = new HashMap<>();
    this.networkTablesMap = new LinkedHashMap<>();
    this.observationMap = new LinkedHashMap<>();
    this.observed = new HashMap<>();
    this.constraints = new ArrayList<>();
    this.solved = false;
  }

  /**
   * Retrieves the primary {@link ProbabilityTable} associated with a {@link Node} identified by its
   * ID. <br>
   * The table will be conditional (CPT) if the node has parents, otherwise it will be represented
   * by a marginal table.
   *
   * @param nodeID The ID of the node whose network table is requested.
   * @param <T> The class of the node ID.
   * @return The {@link ProbabilityTable} for the specified node.
   */
  public <T> ProbabilityTable getNetworkTable(T nodeID) {
    return networkTablesMap.get(nodeIDsMap.get(nodeID));
  }

  /**
   * Retrieves the observed {@link MarginalTable} associated with a {@link Node} identified by its
   * ID.
   *
   * @param nodeID The ID of the node whose network table is requested.
   * @param <T> The class of the node ID.
   * @return The {@link ProbabilityTable} for the specified node.
   */
  public <T> MarginalTable getObservedTable(T nodeID) {
    return observationMap.get(nodeIDsMap.get(nodeID));
  }
}
