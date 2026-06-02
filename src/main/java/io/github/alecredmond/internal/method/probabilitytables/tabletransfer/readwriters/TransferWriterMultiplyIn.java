package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory.TransferWriterMultiplyInFactory;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class TransferWriterMultiplyIn extends VectorIterator<VectorOdometer>
    implements TransferIterator {
  private final double[] transferArray;
  private final int[] tIndex = {0};

  public TransferWriterMultiplyIn(
      ProbabilityVector write, double[] transferArray, TransferWriterMultiplyInFactory logic) {
    super(write, logic, VectorOdometer::new);
    this.transferArray = transferArray;
  }

  @Override
  public void performRun() {
    tIndex[0] = 0;
    double[] probabilities = controller.getOdometer().getProbabilities();
    iterateOuter(
        () -> {
          double ratio = transferArray[tIndex[0]];
          iterateInner((o, i) -> probabilities[i] *= ratio);
          tIndex[0]++;
        });
  }
}
