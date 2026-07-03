package io.github.alecredmond.internal.method.constraints.types.jointconstraint;

import io.github.alecredmond.export.application.constraints.JointProbabilityConstraint;
import io.github.alecredmond.export.serialization.constraint.SerializedJointProbabilityConstraint;
import io.github.alecredmond.export.serialization.constraint.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSerializer;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;
import java.util.ArrayList;

public class JointConstraintSerializer implements ConstraintSerializer<JointProbabilityConstraint> {

  @Override
  public SerializedProbabilityConstraint<JointProbabilityConstraint> serialize(
      JointProbabilityConstraint constraint) {
    return new SerializedJointProbabilityConstraint(
        SerializerUtils.serializeNodeStates(constraint.getEventStates()),
        SerializerUtils.serializeNodeStates(constraint.getConditionStates()),
        constraint.getProbability());
  }

  @Override
  public JointProbabilityConstraint deSerialize(
      SerializedProbabilityConstraint<JointProbabilityConstraint> serialized,
      SerializationData serializationData) {
    SerializedJointProbabilityConstraint sc = (SerializedJointProbabilityConstraint) serialized;
    return new JointProbabilityConstraint(
        SerializerUtils.deSerializeNodeStates(sc.getEventIds(), ArrayList::new, serializationData),
        SerializerUtils.deSerializeNodeStates(
            sc.getConditionIds(), ArrayList::new, serializationData),
        sc.getProbability());
  }
}
