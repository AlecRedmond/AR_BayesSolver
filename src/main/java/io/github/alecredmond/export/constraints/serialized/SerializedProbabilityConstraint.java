package io.github.alecredmond.export.constraints.serialized;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;

/**
 * A {@link Serializable} representation of a {@link ProbabilityConstraint}. {@code
 * SerializedProbabilityConstraint} records are flattened to contain only the identifiers of the
 * relevant {@link NodeState}s and the constraint's probability. Compatible with File I/O
 * operations.
 *
 * @see ProbabilityConstraint
 * @see SerializedConditionalConstraint
 * @see SerializedJointProbabilityConstraint
 * @see SerializedMarginalConstraint
 * @see SerializedSumConstraint
 * @author Alec Redmond
 */
public interface SerializedProbabilityConstraint extends Serializable {}
