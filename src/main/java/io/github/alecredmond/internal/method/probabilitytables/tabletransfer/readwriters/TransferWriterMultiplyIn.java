package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory.TransferWriterMultiplyInFactory;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import java.util.concurrent.atomic.AtomicInteger;

public class TransferWriterMultiplyIn extends VectorIterator implements TransferIterator {
  private final double[] transferArray;

  public TransferWriterMultiplyIn(
      ProbabilityVector write, double[] transferArray, TransferWriterMultiplyInFactory logic) {
    super(write, logic, VectorOdometer::new);
    this.transferArray = transferArray;
  }

  @Override
  public void performRun() {
    AtomicInteger tIndex = new AtomicInteger();
    double[] probabilities = controller.getOdometer().getProbabilities();
    iterateOuter(
        () -> {
          double ratio = transferArray[tIndex.getAndAdd(1)];
          iterateInner((o, i) -> probabilities[i] = probabilities[i] * ratio);
        });
  }
}
