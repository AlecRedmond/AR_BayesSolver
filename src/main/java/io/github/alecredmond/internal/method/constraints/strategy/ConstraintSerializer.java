package io.github.alecredmond.internal.method.constraints.strategy;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.serialization.SerializationData;

public interface ConstraintSerializer<
    T extends ProbabilityConstraint, S extends SerializedProbabilityConstraint<T>> {
  S serialize(T constraint);

  T deSerializeAndValidate(
      SerializedProbabilityConstraint<?> serialized,
      ConstraintValidator<T, ?> validator,
      SerializationData serializationData);

  T deSerialize(S serialized, SerializationData serializationData);
}
