package io.github.alecredmond.internal.method.probabilitytables.transfer.readwriters;

import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.BaseVectorIterator;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;
import java.util.concurrent.atomic.AtomicInteger;

public class TransferWriterMultiplyIn extends BaseVectorIterator implements VectorIterator {
  private final double[] transferArray;

  public TransferWriterMultiplyIn(VectorOdometer vectorOdometer, double[] transferArray) {
    super(vectorOdometer);
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
