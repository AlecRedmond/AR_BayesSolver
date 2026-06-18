package io.github.alecredmond.export.application.network;

import static io.github.alecredmond.internal.method.node.NodeUtils.formatIDsToString;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.network.BayesianNetworkBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NonNull;

/**
 * Data class for {@link BayesianNetworkBuilder} which defines the properties of a single {@link
 * Node} to be constructed in a {@link BayesianNetwork}.
 *
 * @see BayesianNetworkBuilder
 * @see Node
 * @see BayesianNetwork
 * @author Alec Redmond
 */
@Data
public class NetworkBuilderNode {
  private final Serializable nodeId;
  private final List<? extends Serializable> stateIds;
  private final List<? extends Serializable> parentNodeIds;
  private final List<? extends Serializable> cptNodeOrder;
  private final double[] cptValues;

  /**
   * Constructs a new {@code NetworkBuilderNode} for a root (unparented) {@link Node}.
   *
   * @param nodeId the identifier for the root {@link Node}.
   * @param stateIds the identifiers for the {@link NodeState} values associated with the {@link
   *     Node}.
   * @param <T> the {@link Serializable} type of the identifiers.
   * @throws NullPointerException if any input parameter is {@code null}.
   * @throws IllegalArgumentException if {@code stateIds} is null or empty.
   */
  public <T extends Serializable> NetworkBuilderNode(@NonNull T nodeId, @NonNull List<T> stateIds) {
    assertStateIdsNotEmpty(nodeId, stateIds);
    this.nodeId = nodeId;
    this.stateIds = stateIds;
    this.parentNodeIds = null;
    this.cptNodeOrder = null;
    this.cptValues = null;
  }

  private static <T extends Serializable> void assertStateIdsNotEmpty(T id, List<T> stateIds) {
    if (!stateIds.isEmpty()) return;
    throw new IllegalArgumentException("STATE IDs EMPTY FOR NODE ID : %s".formatted(id));
  }

  /**
   * Constructs a new {@code NetworkBuilderNode} for a root (unparented) {@link Node}.
   *
   * <p>For root nodes, the length of the {@code cptValues} array must exactly match the size of the
   * {@code stateIds} list. The probabilities must be provided in the same order as the declared
   * states. For example:
   *
   * <pre>{@code
   * String nodeId = "RAIN";
   * List<String> stateIds = List.of("RAIN:T", "RAIN:F");
   * double[] rainCPT = {0.2, 0.8}; // 20% chance of RAIN:T, 80% chance of RAIN:F
   * bayesianNetworkBuilder.addRootNode(nodeId, stateIds, rainCPT);
   * }</pre>
   *
   * @param nodeId the identifier for the root {@link Node}.
   * @param stateIds the identifiers for the {@link NodeState} values associated with the {@link
   *     Node}.
   * @param cptValues the marginal probabilities for this root {@link Node}.
   * @param <T> the {@link Serializable} type of the identifiers.
   * @throws NullPointerException if any input parameter is {@code null}.
   * @throws IllegalArgumentException if {@code stateIds} is null or empty.
   */
  public <T extends Serializable> NetworkBuilderNode(
      T nodeId, List<T> stateIds, double[] cptValues) {
    assertStateIdsNotEmpty(nodeId, stateIds);
    this.nodeId = nodeId;
    this.stateIds = stateIds;
    this.parentNodeIds = List.of();
    this.cptNodeOrder = List.of(this.nodeId);
    this.cptValues = cptValues;
  }

  /**
   * Constructs a new {@code NetworkBuilderNode} for a conditional (parented) {@link Node}. If the
   * {@code parentNodeIds} parameter is an empty list, this constructor works identically to {@link
   * #NetworkBuilderNode(Serializable, List)}.
   *
   * @param nodeId the identifier for this {@link Node}.
   * @param stateIds the identifiers for the {@link NodeState} values associated with this {@link
   *     Node}.
   * @param parentNodeIds the identifiers of the parent {@link Node}s.
   * @param <T> the {@link Serializable} type of the identifiers.
   * @throws NullPointerException if any input parameter is {@code null}.
   * @throws IllegalArgumentException if {@code stateIds} is null or empty.
   */
  public <T extends Serializable> NetworkBuilderNode(
      @NonNull T nodeId, @NonNull List<T> stateIds, @NonNull List<T> parentNodeIds) {
    assertStateIdsNotEmpty(nodeId, stateIds);
    this.nodeId = nodeId;
    this.stateIds = stateIds;
    this.parentNodeIds = parentNodeIds;
    this.cptNodeOrder = null;
    this.cptValues = null;
  }

  /**
   * Constructs a new {@code NetworkBuilderNode} for a conditional (parented) {@link Node}.
   *
   * <p>For conditional nodes, the length of the {@code cptValues} array must equal the product of
   * the number of states for this node and all of its parents.
   *
   * <p>The correct ordering of the {@code cptValues} array is determined by the {@code
   * cptNodeOrder} parameter, which must contain the identifiers of this node and all its parents.
   * The builder iterates through the Cartesian product of the states for these nodes. The iteration
   * cycles fastest through the right-most (last) node in the {@code cptNodeOrder} list,
   * incrementing the node to its left whenever it overflows.
   *
   * <p>Consider three nodes representing {@code P(WET_GRASS | RAIN, SPRINKLER)}, each with {@code
   * :T} and {@code :F} states:
   *
   * <pre>{@code
   * String nodeId = "WET_GRASS";
   * List<String> stateIds = List.of("WET_GRASS:T", "WET_GRASS:F");
   * // If the longest stride is "RAIN", and the shortest is "WET_GRASS":
   * List<String> cptNodeOrder = List.of("RAIN", "SPRINKLER", "WET_GRASS");
   * double[] wetGrassCpt = new double[]{
   * 0.99, // RAIN:T, SPRINKLER:T, WET_GRASS:T
   * 0.01, // RAIN:T, SPRINKLER:T, WET_GRASS:F
   * 0.80, // RAIN:T, SPRINKLER:F, WET_GRASS:T
   * 0.20, // RAIN:T, SPRINKLER:F, WET_GRASS:F
   * 0.90, // RAIN:F, SPRINKLER:T, WET_GRASS:T
   * 0.10, // RAIN:F, SPRINKLER:T, WET_GRASS:F
   * 0.00, // RAIN:F, SPRINKLER:F, WET_GRASS:T
   * 1.00  // RAIN:F, SPRINKLER:F, WET_GRASS:F
   * };
   * bayesianNetworkBuilder.addNode(nodeId, stateIds, cptNodeOrder, wetGrassCpt);
   * }</pre>
   *
   * This can also be used to construct a root node if only the {@code nodeId} is contained within
   * the {@code cptOrderList}. In this case, it works identically to {@link
   * #NetworkBuilderNode(Serializable, List, double[])}.
   *
   * @param nodeId the identifier for this {@link Node}.
   * @param stateIds the identifiers for the {@link NodeState} values associated with this {@link
   *     Node}.
   * @param cptNodeOrder the identifiers of the node and its parents, establishing the iteration
   *     hierarchy used for the {@code cptValues} array.
   * @param cptValues the conditional probabilities mapping to the Cartesian product of the node
   *     states.
   * @param <T> the {@link Serializable} type of the identifiers.
   * @throws NullPointerException if any input parameter is {@code null}.
   * @throws IllegalArgumentException if {@code stateIds} or empty, or if {@code cptStrideOrderDesc}
   *     does not contain the {@code nodeId}.
   */
  public <T extends Serializable> NetworkBuilderNode(
      @NonNull T nodeId,
      @NonNull List<T> stateIds,
      @NonNull List<T> cptNodeOrder,
      @NonNull double[] cptValues) {
    assertStateIdsNotEmpty(nodeId, stateIds);
    this.nodeId = nodeId;
    this.stateIds = stateIds;
    this.parentNodeIds = buildParentIds(nodeId, cptNodeOrder);
    this.cptNodeOrder = cptNodeOrder;
    this.cptValues = cptValues;
  }

  private <T extends Serializable> List<T> buildParentIds(T id, List<T> cptStrideOrderDesc) {
    List<T> parents = new ArrayList<>(cptStrideOrderDesc);
    if (parents.remove(id)) {
      return parents.isEmpty() ? null : parents;
    }
    throw new IllegalArgumentException(
        "cptNodeOrder list {%s} does not contain node id {%s}!"
            .formatted(formatIDsToString(cptStrideOrderDesc), id));
  }
}
