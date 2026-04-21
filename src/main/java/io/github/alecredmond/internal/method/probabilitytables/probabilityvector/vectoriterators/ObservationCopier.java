package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import java.util.Arrays;

public class ObservationCopier extends BaseVectorIterator implements VectorIterator {
  private final ProbabilityVector backupVector;

  public ObservationCopier(VectorOdometer vectorOdometer, ProbabilityVector backupVector) {
    super(vectorOdometer);
    this.backupVector = backupVector;
  }

  @Override
  public void performRun() {
    double[] observed = vectorOdometer.getProbabilities();
    Arrays.fill(observed, 0.0);
    double[] backup = backupVector.getProbabilities();
    iterateInner((o, i) -> observed[i] = backup[i]);
  }
}
