package io.github.alecredmond.export.node.serialized;

import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.List;

/**
 * A {@link Serializable} representation of a {@link Node}. The serialization process flattens the
 * {@link Node} to its constituent identifiers. Used in File I/O operations.
 *
 * @param id the identifier of the {@link Node}.
 * @param stateIds the identifiers of the {@link Node}'s {@link NodeState}s.
 * @param parentIds the identifiers of the {@link Node}'s parents.
 * @param childIds the identifiers of the {@link Node}'s children.
 * @see Node
 * @see SerializedBayesianNetwork
 * @author Alec Redmond
 */
public record SerializedNode(
    Serializable id,
    List<Serializable> stateIds,
    List<Serializable> parentIds,
    List<Serializable> childIds)
    implements Serializable {}
