package io.github.alecredmond.export.serialization.constraint;

import io.github.alecredmond.export.application.constraints.ConditionalConstraint;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SerializedConditionalConstraint
    implements SerializedProbabilityConstraint<ConditionalConstraint> {
  private Serializable eventStateId;
  private List<Serializable> conditionStateIds;
  private double probability;

  public SerializedConditionalConstraint(
      Serializable eventStateId, List<Serializable> conditionStateIds, double probability) {
    this.eventStateId = eventStateId;
    this.conditionStateIds = conditionStateIds;
    this.probability = probability;
  }

  @Override
  public Class<ConditionalConstraint> getConstraintClass() {
    return ConditionalConstraint.class;
  }
}
