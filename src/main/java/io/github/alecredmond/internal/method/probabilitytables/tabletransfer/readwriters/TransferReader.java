package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory.TransferReaderFactory;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import lombok.Getter;

@Getter
public class TransferReader extends VectorIterator implements TransferIterator {
  private final double[] transferArray;

  public TransferReader(
      ProbabilityVector read, double[] transferArray, TransferReaderFactory logic) {
    super(read, logic, VectorOdometer::new);
    this.transferArray = transferArray;
  }

  @Override
  public void performRun() {
    AtomicInteger tIndex = new AtomicInteger();
    DoubleAdder adder = new DoubleAdder();
    double[] probabilities = controller.getOdometer().getProbabilities();
    iterateOuter(
        () -> {
          iterateInner((o, i) -> adder.add(probabilities[i]));
          transferArray[tIndex.getAndAdd(1)] = adder.sumThenReset();
        });
  }
}
