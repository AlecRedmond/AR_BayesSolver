package io.github.alecredmond.internal.method.probabilitytables.transfer.readwriters;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerResetLogic;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;

import java.util.concurrent.atomic.AtomicInteger;

public class TransferWriterMultiplyIn extends VectorIterator implements TransferIterator {
  private final double[] transferArray;

  public TransferWriterMultiplyIn(
      ProbabilityVector write, double[] transferArray, OdometerResetLogic logic) {
    super(write, logic);
    this.transferArray = transferArray;
  }

  @Override
  public void performRun() {
    AtomicInteger tIndex = new AtomicInteger();
    double[] probabilities = vectorOdometer.getProbabilities();
    iterateOuter(
        () -> {
          double ratio = transferArray[tIndex.getAndAdd(1)];
          iterateInner((o, i) -> probabilities[i] = probabilities[i] * ratio);
        });
  }
}
