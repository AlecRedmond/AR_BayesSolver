package io.github.alecredmond.export.serialization.constraint;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class SerializedConditionalConstraint extends SerializedProbabilityConstraint implements Serializable {
  private List<Serializable> conditionStateIds;

  public SerializedConditionalConstraint(
      Serializable eventStateId, List<Serializable> conditionStateIds, double probability) {
    super(eventStateId, probability);
    this.conditionStateIds = conditionStateIds;
  }
}
