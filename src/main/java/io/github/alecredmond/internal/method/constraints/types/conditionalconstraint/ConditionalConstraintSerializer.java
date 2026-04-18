package io.github.alecredmond.internal.method.constraints.types.conditionalconstraint;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.serialization.constraint.SerializedConditionalConstraint;
import io.github.alecredmond.export.serialization.constraint.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSerializer;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;
import java.util.ArrayList;

public class ConditionalConstraintSerializer
    implements ConstraintSerializer<ConditionalConstraint> {

  @Override
  public SerializedConditionalConstraint serialize(ConditionalConstraint constraint) {
    return new SerializedConditionalConstraint(
        constraint.getEventState().getId(),
        SerializerUtils.serializeNodeStates(constraint.getConditionStates()),
        constraint.getProbability());
  }

  @Override
  public ConditionalConstraint deSerialize(
      SerializedProbabilityConstraint<ConditionalConstraint> serialized,
      SerializationData serializationData) {
    return new ConditionalConstraint(
        serializationData
            .getNodeStateIdMap()
            .get(((SerializedConditionalConstraint) serialized).getEventStateId()),
        SerializerUtils.deSerializeNodeStates(
            ((SerializedConditionalConstraint) serialized).getConditionStateIds(),
            ArrayList::new,
            serializationData),
        ((SerializedConditionalConstraint) serialized).getProbability());
  }
}
