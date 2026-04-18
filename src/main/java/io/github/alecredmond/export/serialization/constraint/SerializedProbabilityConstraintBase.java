package io.github.alecredmond.export.serialization.constraint;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class SerializedProbabilityConstraintBase {
  protected Serializable eventStateId;
  protected double probability;

}
