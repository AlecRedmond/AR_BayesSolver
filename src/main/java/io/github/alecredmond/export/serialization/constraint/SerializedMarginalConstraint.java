package io.github.alecredmond.export.serialization.constraint;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class SerializedMarginalConstraint extends SerializedProbabilityConstraint implements Serializable {
  public SerializedMarginalConstraint(Serializable eventStateId, double probability) {
    super(eventStateId, probability);
  }
}
