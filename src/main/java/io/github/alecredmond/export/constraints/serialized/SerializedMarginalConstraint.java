package io.github.alecredmond.export.constraints.serialized;

import io.github.alecredmond.export.constraints.MarginalConstraint;
import io.github.alecredmond.internal.method.constraints.ConstraintType;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SerializedMarginalConstraint
    implements SerializedProbabilityConstraint<MarginalConstraint> {
  private Serializable eventStateId;
  private double probability;

  public SerializedMarginalConstraint(Serializable eventStateId, double probability) {
    this.eventStateId = eventStateId;
    this.probability = probability;
  }

  @Override
  public Class<MarginalConstraint> getConstraintClass() {
    return MarginalConstraint.class;
  }

  @Override
  public String getConstraintType() {
    return ConstraintType.MARGINAL.name();
  }
}
