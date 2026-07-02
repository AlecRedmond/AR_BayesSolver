package io.github.alecredmond.internal.method.constraints.types.sumconstraint;

import io.github.alecredmond.export.application.constraints.SumProbabilityConstraint;
import io.github.alecredmond.export.serialization.constraint.SerializedProbabilityConstraint;
import io.github.alecredmond.export.serialization.constraint.SerializedSumConstraint;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSerializer;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;
import java.util.ArrayList;

public class SumConstraintSerializer implements ConstraintSerializer<SumProbabilityConstraint> {
  @Override
  public SerializedProbabilityConstraint<SumProbabilityConstraint> serialize(
      SumProbabilityConstraint constraint) {
    return new SerializedSumConstraint(
        SerializerUtils.serializeNodeStates(constraint.getEventStates()),
        SerializerUtils.serializeNodeStates(constraint.getConditionStates()),
        constraint.getProbability());
  }

  @Override
  public SumProbabilityConstraint deSerialize(
      SerializedProbabilityConstraint<SumProbabilityConstraint> serialized,
      SerializationData serializationData) {
    SerializedSumConstraint sc = (SerializedSumConstraint) serialized;
    return new SumProbabilityConstraint(
        SerializerUtils.deSerializeNodeStates(sc.getEventIds(), ArrayList::new, serializationData),
        SerializerUtils.deSerializeNodeStates(
            sc.getConditionIds(), ArrayList::new, serializationData),
        sc.getProbability());
  }
}
