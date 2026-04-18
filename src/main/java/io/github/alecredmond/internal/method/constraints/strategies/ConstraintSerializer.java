package io.github.alecredmond.internal.method.constraints.strategies;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.serialization.constraint.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.serialization.SerializationData;

public interface ConstraintSerializer<T extends ProbabilityConstraint> {
  SerializedProbabilityConstraint<T> serialize(T constraint);

  T deSerialize(SerializedProbabilityConstraint<T> serialized, SerializationData serializationData);
}
