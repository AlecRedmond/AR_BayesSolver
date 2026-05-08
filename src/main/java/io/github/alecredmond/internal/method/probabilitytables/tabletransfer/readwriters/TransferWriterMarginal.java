package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory.TransferWriterMarginalFactory;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

public class TransferWriterMarginal extends VectorIterator implements TransferIterator {
  private final double[] transferArray;

  public TransferWriterMarginal(
      ProbabilityVector write, double[] transferArray, TransferWriterMarginalFactory logic) {
    super(write, logic, VectorOdometer::new);
    this.transferArray = transferArray;
  }

  @Override
  public void performRun() {
    AtomicInteger tIndex = new AtomicInteger();
    DoubleAdder adder = new DoubleAdder();
    double[] probabilities = controller.getOdometer().getProbabilities();
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
