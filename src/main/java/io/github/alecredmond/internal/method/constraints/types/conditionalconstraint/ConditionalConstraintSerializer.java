package io.github.alecredmond.internal.method.constraints.types.conditionalconstraint;

import io.github.alecredmond.export.constraints.ConditionalConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedConditionalConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.base.ConstraintSerializerBase;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;
import java.util.ArrayList;

public class ConditionalConstraintSerializer
    extends ConstraintSerializerBase<ConditionalConstraint, SerializedConditionalConstraint> {

  @Override
  public SerializedConditionalConstraint serialize(ConditionalConstraint constraint) {
    return new SerializedConditionalConstraint(
        constraint.getEventState().getId(),
        SerializerUtils.serializeNodeStates(constraint.getConditionStates()),
        constraint.getProbability());
  }

  @Override
  public ConditionalConstraint deSerialize(
      SerializedConditionalConstraint serialized, SerializationData serializationData) {
    return new ConditionalConstraint(
        serializationData.getNodeStateIdMap().get(serialized.eventStateId()),
        SerializerUtils.deSerializeNodeStates(
            serialized.conditionStateIds(), ArrayList::new, serializationData),
        serialized.probability());
  }

  @Override
  public SerializedConditionalConstraint safeCast(SerializedProbabilityConstraint serialized) {
    return serialized instanceof SerializedConditionalConstraint cast ? cast : null;
  }
}
