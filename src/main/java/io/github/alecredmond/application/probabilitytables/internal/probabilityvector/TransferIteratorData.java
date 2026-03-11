package io.github.alecredmond.application.probabilitytables.internal.probabilityvector;

import io.github.alecredmond.application.probabilitytables.export.probabilityvector.ProbabilityVector;
import lombok.Data;

@Data
public class TransferIteratorData {
  private final ProbabilityVector tableVector;
  private final VectorCombinationKey transferKey;
  private final int iterationSteps;
}
