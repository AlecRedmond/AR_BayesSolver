package io.github.alecredmond.internal.method.probabilitytables.transfer.readwriters;

import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.BaseVectorIterator;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

public class TransferWriterMarginal extends BaseVectorIterator implements VectorIterator {
  private final double[] transferArray;

  public TransferWriterMarginal(VectorOdometer odometer, double[] transferArray) {
    super(odometer);
    this.transferArray = transferArray;
  }

  @Override
  public void performRun() {
    AtomicInteger tIndex = new AtomicInteger();
    DoubleAdder adder = new DoubleAdder();
    double[] probabilities = vectorOdometer.getProbabilities();
    iterateOuter(
        (() -> {
          iterateInner((o, i) -> adder.add(probabilities[i]));
          double actual = adder.sumThenReset();
          double expected = transferArray[tIndex.getAndAdd(1)];
          double ratio = actual == 0.0 ? 0.0 : expected / actual;
          iterateInner((o, i) -> probabilities[i] = probabilities[i] * ratio);
        }));
  }
}
