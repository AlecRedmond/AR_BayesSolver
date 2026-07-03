package io.github.alecredmond.export.application.probabilitytables.cptentry;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import java.util.Set;

/**
 * An immutable representation of a single Conditional Probability Table (CPT) entry within a {@link
 * NetworkTable}.
 *
 * @param conditionStates the conditioning {@link NodeState}s active on this entry.
 * @param eventState the measured {@link NodeState}.
 * @param probability the probability of {@code eventState}, conditional on {@code conditionStates}.
 * @param index the index of the entry in the probability {@code double[]} array of the associated
 *     {@link NetworkTable}.
 * @author Alec Redmond
 */
public record CptEntry(
    Set<NodeState> conditionStates, NodeState eventState, double probability, int index) {}
