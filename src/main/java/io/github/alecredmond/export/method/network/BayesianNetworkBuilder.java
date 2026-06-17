package io.github.alecredmond.export.method.network;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.export.application.network.NetworkBuilderNode;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.network.NetworkInputBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * A builder object to streamline the construction of a {@link BayesianNetwork}. {@code
 * BayesianNetworkBuilder} allows information about the network to be input on a per-{@link Node}
 * level, using:
 *
 * <ul>
 *   <li>The {@link Serializable} id for the {@link Node}
 *   <li>The {@link Serializable} ids for the node's {@link NodeState}s
 *   <li>(Optionally) The {@link Serializable} ids of the node's parent nodes
 *   <li>(Optionally) The CPT values for the node, in the form of a {@code double[]} array.
 * </ul>
 *
 * This bypasses the strict input order which is necessary when manually building a {@link
 * BayesianNetwork} from the interface.
 *
 * @see BayesianNetwork
 * @author Alec Redmond
 */
@Data
public class BayesianNetworkBuilder {
  private String networkName;
  private List<NetworkBuilderNode> nodeInputs = new ArrayList<>();

  /**
   * Constructs a new {@code BayesianNetworkBuilder}, with the default network name {@code "UNNAMED
   * NETWORK"}.
   */
  public BayesianNetworkBuilder() {
    this.networkName = "UNNAMED NETWORK";
  }

  /**
   * Constructs a new {@code BayesianNetworkBuilder}.
   *
   * @param networkName the name to be given to the {@link BayesianNetwork}.
   */
  public BayesianNetworkBuilder(String networkName) {
    this.networkName = networkName;
  }

  /**
   * Constructs and returns the {@link BayesianNetwork} using the current values in {@link
   * #nodeInputs}.
   *
   * @return a new {@link BayesianNetwork}
   * @throws BayesNetIDException if any id was incorrectly mapped or duplicated.
   * @throws NetworkStructureException if the network has disconnected sections, or if the
   *     parent/child relationships would form a cycle.
   * @throws ConstraintValidationException if a CPT array was not the correct length, or if the CPT
   *     array was in an illegal configuration.
   */
  public BayesianNetwork build() {
    return new NetworkInputBuilder().buildNetwork(networkName, nodeInputs);
  }

  /**
   * Adds a {@link NetworkBuilderNode} to this {@code BayesianNetworkBuilder} representing a root
   * (unparented) {@link Node}.
   *
   * @param nodeId the id for the root {@link Node}.
   * @param stateIds the ids for the {@link NodeState} values associated with the {@link Node}.
   * @param <T> the {@link Serializable} class of the ids.
   * @return this instance for chaining.
   * @throws IllegalArgumentException if {@code stateIds} is an empty list.
   */
  public <T extends Serializable> BayesianNetworkBuilder addRootNode(T nodeId, List<T> stateIds) {
    nodeInputs.add(new NetworkBuilderNode(nodeId, stateIds));
    return this;
  }

  /**
   * Adds a {@link NetworkBuilderNode} to this {@code BayesianNetworkBuilder} representing a root
   * (unparented) {@link Node}.
   *
   * <p>This method allows CPT values to be entered as a {@code double[]} array. For root nodes, the
   * length of this array should be the size of the {@code stateIds} list, and maintain the same
   * order. A valid example follows:
   *
   * <pre>{@code
   * String nodeId = "RAIN";
   * List<String> stateIds = List.of("RAIN:T","RAIN:F");
   * double[] rainCPT = {0.2,0.8}; //20% chance of RAIN:T, 80% of RAIN:F
   * bayesianNetworkBuilder.addRootNode(nodeId,stateIds,rainCPT);
   * }</pre>
   *
   * @param nodeId the id for the root {@link Node}.
   * @param stateIds the ids for the {@link NodeState} values associated with the {@link Node}.
   * @param cptValues the CPT array for this root {@link Node}.
   * @param <T> the {@link Serializable} class of the ids.
   * @return this instance for chaining.
   * @throws IllegalArgumentException if {@code stateIds} is an empty list.
   */
  public <T extends Serializable> BayesianNetworkBuilder addRootNode(
      T nodeId, List<T> stateIds, double[] cptValues) {
    nodeInputs.add(new NetworkBuilderNode(nodeId, stateIds, cptValues));
    return this;
  }

  /**
   * Adds a {@link NetworkBuilderNode} to this {@code BayesianNetworkBuilder} representing a
   * conditional (parented) {@link Node}.
   *
   * @param nodeId the id for the {@link Node}.
   * @param stateIds the ids for the {@link NodeState} values associated with the {@link Node}.
   * @param parentIds the ids of the parent {@link Node}s. These must also be declared in this
   *     {@code BayesianNetworkBuilder} instance.
   * @param <T> the {@link Serializable} class of the ids.
   * @return this instance for chaining.
   * @throws IllegalArgumentException if {@code stateIds} is an empty list.
   */
  public <T extends Serializable> BayesianNetworkBuilder addNode(
      T nodeId, List<T> stateIds, List<T> parentIds) {
    nodeInputs.add(new NetworkBuilderNode(nodeId, stateIds, parentIds));
    return this;
  }

  /**
   * Adds a {@link NetworkBuilderNode} to this {@code BayesianNetworkBuilder} representing a
   * conditional (parented) {@link Node}.
   *
   * <p>This method allows CPT values to be entered as a {@code double[]} array. For conditional
   * nodes, the length of this array should be equal to the product of all {@code stateIds} list
   * sizes for itself and its parents. The order this follows is associated with the {@code
   * cptStrideOrderDesc} parameter, which should include the {@code nodeId} and the ids of all its
   * parent nodes.
   *
   * <p>The Cartesian product of the {@code stateIds} for all node ids involved will be cycled
   * through in order, starting with the last node in {@code cptStrideOrderDesc}, with every
   * overflow incrementing the position to the left. For example, consider 3 nodes, each having
   * {@code List.of("{nodeId}:T","{nodeId}:F")} as their stateIds. The order of the CPT would be as
   * follows:
   *
   * <pre>{@code
   * String nodeId = "WET_GRASS";
   * List<String> stateIds = List.of("WET_GRASS:T","WET_GRASS:F");
   * List<String> cptStrideOrderDesc = List.of("RAIN","SPRINKLER","WET_GRASS");
   * // This represents P(WET_GRASS|RAIN,SPRINKLER)
   * // The longest stride will be "RAIN", the shortest will be "WET_GRASS"
   * double[] wetGrassCpt = new double[]{
   *     0.99, // RAIN:T, SPRINKLER:T, WET_GRASS:T
   *     0.01, // RAIN:T, SPRINKLER:T, WET_GRASS:F
   *     0.80, // RAIN:T, SPRINKLER:F, WET_GRASS:T
   *     0.20, // RAIN:T, SPRINKLER:F, WET_GRASS:F
   *     0.90, // RAIN:F, SPRINKLER:T, WET_GRASS:T
   *     0.10, // RAIN:F, SPRINKLER:T, WET_GRASS:F
   *     0.00, // RAIN:F, SPRINKLER:F, WET_GRASS:T
   *     1.00  // RAIN:F, SPRINKLER:F, WET_GRASS:F
   * }
   * bayesianNetworkBuilder.addNode(nodeId,stateIds,cptStrideOrderDesc,wetGrassCpt);
   * }</pre>
   *
   * @param nodeId the id for the {@link Node}.
   * @param stateIds the ids for the {@link NodeState} values associated with the {@link Node}.
   * @param cptStrideOrderDesc the ids of the {@link Node} and its parents, ordered as described in
   *     the method documentation.
   * @param <T> the {@link Serializable} class of the ids.
   * @return this instance for chaining.
   * @throws IllegalArgumentException if {@code stateIds} is an empty list, or if {@code
   *     cptStrideOrderDesc} does not contain {@code nodeId}.
   */
  public <T extends Serializable> BayesianNetworkBuilder addNode(
      T nodeId, List<T> stateIds, List<T> cptStrideOrderDesc, double[] cptValues) {
    nodeInputs.add(new NetworkBuilderNode(nodeId, stateIds, cptStrideOrderDesc, cptValues));
    return this;
  }
}
