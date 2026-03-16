package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.serialization.constraint.SerializedConditionalConstraint;
import io.github.alecredmond.export.serialization.constraint.SerializedMarginalConstraint;
import io.github.alecredmond.export.serialization.constraint.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;

import java.util.ArrayList;

public class ConstraintSerializer {

  public SerializedProbabilityConstraint serialize(ProbabilityConstraint constraint) {
    return switch (constraint) {
      case MarginalConstraint mc -> serializeMarginal(mc);
      case ConditionalConstraint cc -> serializeConditional(cc);
      default -> throw new IllegalStateException("Unexpected value: " + constraint);
    };
  }

  private SerializedMarginalConstraint serializeMarginal(MarginalConstraint constraint) {
    return new SerializedMarginalConstraint(
        constraint.getEventState().getId(), constraint.getProbability());
  }

  private SerializedConditionalConstraint serializeConditional(ConditionalConstraint constraint) {
    return new SerializedConditionalConstraint(
        constraint.getEventState().getId(),
        SerializerUtils.serializeNodeStates(constraint.getConditionStates()),
        constraint.getProbability());
  }

  public ProbabilityConstraint deSerialize(
          SerializedProbabilityConstraint sto, SerializationData data) {
    return switch (sto) {
      case SerializedMarginalConstraint marginal -> deSerializeMarginal(marginal, data);
      case SerializedConditionalConstraint conditional -> deSerializeConditional(conditional, data);
      default -> throw new IllegalStateException("Unexpected value: " + sto);
    };
  }

  private ProbabilityConstraint deSerializeMarginal(
          SerializedMarginalConstraint marginal, SerializationData data) {
    return new MarginalConstraint(
        data.getNodeStateIdMap().get(marginal.getEventStateId()), marginal.getProbability());
  }

  private ProbabilityConstraint deSerializeConditional(
          SerializedConditionalConstraint conditional, SerializationData data) {
    return new ConditionalConstraint(
        data.getNodeStateIdMap().get(conditional.getEventStateId()),
        SerializerUtils.deSerializeNodeStates(conditional.getConditionStateIds(), ArrayList::new, data),
        conditional.getProbability());
  }
}
