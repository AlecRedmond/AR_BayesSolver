package io.github.alecredmond.internal.application.probabilitytables.probabilityvector;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import lombok.Data;

@Data
public class TransferIteratorData {
  private final ProbabilityVector tableVector;
  private final VectorCombinationKey transferKey;
  private final int iterationSteps;
}
