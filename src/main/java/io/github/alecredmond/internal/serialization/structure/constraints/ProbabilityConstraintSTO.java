package io.github.alecredmond.internal.serialization.structure.constraints;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import io.github.alecredmond.export.application.constraints.MarginalConstraint;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.serialization.mapper.SerializationData;
import io.github.alecredmond.internal.serialization.mapper.SerializationTransferObject;
import java.io.Serializable;

public abstract class ProbabilityConstraintSTO<T extends ProbabilityConstraint>
    implements SerializationTransferObject<T> {
  protected Serializable eventStateId;
  protected double probability;


  public static ProbabilityConstraintSTO<?> staticSerialize(ProbabilityConstraint constraint) {
    return switch (constraint) {
      case MarginalConstraint mc -> new MarginalConstraintSTO().serialize(mc);
      case ConditionalConstraint cc -> new ConditionalConstraintSTO().serialize(cc);
      default -> throw new IllegalStateException("Unexpected value: " + constraint);
    };
  }

  public static ProbabilityConstraint staticDeSerialize(
      ProbabilityConstraintSTO<?> sto, SerializationData data) {
    return switch (sto) {
      case MarginalConstraintSTO marginal -> marginal.deSerialize(data);
      case ConditionalConstraintSTO conditional -> conditional.deSerialize(data);
      default -> throw new IllegalStateException("Unexpected value: " + sto);
    };
  }

}
