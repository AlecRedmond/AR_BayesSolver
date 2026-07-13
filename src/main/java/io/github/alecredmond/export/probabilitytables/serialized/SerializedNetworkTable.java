package io.github.alecredmond.export.probabilitytables.serialized;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import java.io.Serializable;
import java.util.List;

/**
 * A {@link Serializable} representation of a {@link NetworkTable} (CPT). {@code
 * SerializedNetworkTable} records are flattened so that every {@link Node} is represented by its
 * identifier. Used for File I/O operations.
 *
 * @param networkNodeId the identifier of the CPT's measured {@link Node}.
 * @param conditionNodeIds the identifier of the CPT's parent {@link Node}s.
 * @param tableName the name identifier of the table.
 * @param probabilities flattened list of conditional probabilities.
 * @see NetworkTable
 * @author Alec Redmond
 */
public record SerializedNetworkTable(
    Serializable networkNodeId,
    List<Serializable> conditionNodeIds,
    Serializable tableName,
    List<Double> probabilities)
    implements Serializable {}
