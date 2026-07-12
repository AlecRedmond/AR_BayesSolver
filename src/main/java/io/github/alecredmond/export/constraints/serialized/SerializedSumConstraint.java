package io.github.alecredmond.export.constraints.serialized;

import io.github.alecredmond.export.constraints.SumProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.ConstraintType;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SerializedSumConstraint
    implements SerializedProbabilityConstraint<SumProbabilityConstraint> {
  private List<Serializable> eventIds;
  private List<Serializable> conditionIds;
  private double probability;

  public SerializedSumConstraint(
      List<Serializable> eventIds, List<Serializable> conditionIds, double probability) {
    this.eventIds = eventIds;
    this.conditionIds = conditionIds;
    this.probability = probability;
  }

  @Override
  public Class<SumProbabilityConstraint> getConstraintClass() {
    return SumProbabilityConstraint.class;
  }

  @Override
  public String getConstraintType() {
    return ConstraintType.SUM.name();
  }
}
