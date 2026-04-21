package io.github.alecredmond.internal.application.probabilitytables.probabilityvector;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TransferIteratorData extends VectorOdometer {
  private ProbabilityVector tableVector;
  private VectorCombinationKey transferKey;
  private int iterationSteps;
}
