package io.github.alecredmond.export.network.serialized;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.node.serialized.SerializedNode;
import io.github.alecredmond.export.probabilitytables.serialized.SerializedNetworkTable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A {@link Serializable} snapshot of a {@link BayesianNetwork}. The serialization process creates a
 * deep copy of the network, replacing all {@link Node} and {@link NodeState} instances with their
 * serializable identifiers. Intended for use in File I/O operations and to save/load the state of
 * the network.
 *
 * @param networkName the name of the {@link BayesianNetwork}.
 * @param serializedNodes a list of serialized nodes from the network.
 * @param serializedCptMap a map pairing each node id to its related CPT.
 * @param serializedConstraints a list of serialized constraints within the network.
 * @param solved a flag indicating whether all constraints have been fitted within the network's
 *     CPTs.
 * @see BayesianNetworkData
 * @see SerializedNode
 * @see SerializedNetworkTable
 * @see SerializedProbabilityConstraint
 * @author Alec Redmond
 */
public record SerializedBayesianNetwork(
    String networkName,
    List<SerializedNode> serializedNodes,
    Map<Serializable, SerializedNetworkTable> serializedCptMap,
    List<SerializedProbabilityConstraint<?>> serializedConstraints,
    boolean solved)
    implements Serializable {}
