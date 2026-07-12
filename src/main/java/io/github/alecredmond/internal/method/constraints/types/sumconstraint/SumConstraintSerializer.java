package io.github.alecredmond.internal.method.constraints.types.sumconstraint;

import io.github.alecredmond.export.constraints.SumProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedSumConstraint;
import io.github.alecredmond.internal.method.constraints.base.ConstraintSerializerBase;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;
import java.util.ArrayList;

public class SumConstraintSerializer
    extends ConstraintSerializerBase<SumProbabilityConstraint, SerializedSumConstraint> {
  @Override
  public SerializedSumConstraint serialize(SumProbabilityConstraint constraint) {
    return new SerializedSumConstraint(
        SerializerUtils.serializeNodeStates(constraint.getEventStates()),
        SerializerUtils.serializeNodeStates(constraint.getConditionStates()),
        constraint.getProbability());
  }

  @Override
  public SumProbabilityConstraint deSerialize(
      SerializedSumConstraint serialized, SerializationData serializationData) {
    return new SumProbabilityConstraint(
        SerializerUtils.deSerializeNodeStates(
            serialized.getEventIds(), ArrayList::new, serializationData),
        SerializerUtils.deSerializeNodeStates(
            serialized.getConditionIds(), ArrayList::new, serializationData),
        serialized.getProbability());
  }

  @Override
  protected SerializedSumConstraint safeCast(SerializedProbabilityConstraint<?> serialized) {
    return serialized instanceof SerializedSumConstraint cast ? cast : null;
  }
}
