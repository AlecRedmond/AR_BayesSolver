package io.github.alecredmond.internal.method.constraints.types.marginalconstraint;

import io.github.alecredmond.export.constraints.MarginalConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedMarginalConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.base.ConstraintSerializerBase;
import io.github.alecredmond.internal.serialization.SerializationData;

public class MarginalConstraintSerializer
    extends ConstraintSerializerBase<MarginalConstraint, SerializedMarginalConstraint> {

  @Override
  public SerializedMarginalConstraint serialize(MarginalConstraint constraint) {
    return new SerializedMarginalConstraint(
        constraint.getEventState().getId(), constraint.getProbability());
  }

  @Override
  public MarginalConstraint deSerialize(
      SerializedMarginalConstraint serialized, SerializationData serializationData) {
    return new MarginalConstraint(
        serializationData.getNodeStateIdMap().get(serialized.getEventStateId()),
        serialized.getProbability());
  }

  @Override
  protected SerializedMarginalConstraint safeCast(SerializedProbabilityConstraint<?> serialized) {
    return serialized instanceof SerializedMarginalConstraint cast ? cast : null;
  }
}
