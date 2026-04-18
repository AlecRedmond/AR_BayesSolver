package io.github.alecredmond.export.serialization.constraint;

import io.github.alecredmond.export.application.constraints.JointProbabilityConstraint;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SerializedJointProbabilityConstraint
    implements SerializedProbabilityConstraint<JointProbabilityConstraint> {
  private List<Serializable> eventIds;
  private List<Serializable> conditionIds;
  private double probability;

  public SerializedJointProbabilityConstraint(
      List<Serializable> eventIds, List<Serializable> conditionIds, double probability) {
    this.eventIds = eventIds;
    this.conditionIds = conditionIds;
    this.probability = probability;
  }

  @Override
  public Class<JointProbabilityConstraint> getConstraintClass() {
    return JointProbabilityConstraint.class;
  }
}
