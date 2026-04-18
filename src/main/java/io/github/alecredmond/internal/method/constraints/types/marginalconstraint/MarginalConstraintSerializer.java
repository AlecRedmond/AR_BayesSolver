package io.github.alecredmond.internal.method.constraints.types.marginalconstraint;

import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.serialization.constraint.SerializedMarginalConstraint;
import io.github.alecredmond.export.serialization.constraint.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSerializer;
import io.github.alecredmond.internal.serialization.SerializationData;

public class MarginalConstraintSerializer implements ConstraintSerializer<MarginalConstraint> {

  @Override
  public SerializedMarginalConstraint serialize(MarginalConstraint constraint) {
    return new SerializedMarginalConstraint(
        constraint.getEventState().getId(), constraint.getProbability());
  }

  @Override
  public MarginalConstraint deSerialize(
      SerializedProbabilityConstraint<MarginalConstraint> serialized,
      SerializationData serializationData) {
    return new MarginalConstraint(
        serializationData
            .getNodeStateIdMap()
            .get(((SerializedMarginalConstraint) serialized).getEventStateId()),
        ((SerializedMarginalConstraint) serialized).getProbability());
  }
}
