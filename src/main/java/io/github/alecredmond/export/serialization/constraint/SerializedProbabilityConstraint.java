package io.github.alecredmond.export.serialization.constraint;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class SerializedProbabilityConstraint implements Serializable {
  protected Serializable eventStateId;
  protected double probability;

  protected SerializedProbabilityConstraint(Serializable eventStateId, double probability) {
    this.eventStateId = eventStateId;
    this.probability = probability;
  }
}
