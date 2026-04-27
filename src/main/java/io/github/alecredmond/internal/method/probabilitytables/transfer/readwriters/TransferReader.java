package io.github.alecredmond.internal.method.probabilitytables.transfer.readwriters;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerResetLogic;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

import lombok.Getter;

@Getter
public class TransferReader extends VectorIterator implements TransferIterator {
  private final double[] transferArray;

  public TransferReader(ProbabilityVector read, double[] transferArray, OdometerResetLogic logic) {
    super(read, logic);
    this.transferArray = transferArray;
  }

  @Override
  public void performRun() {
    AtomicInteger tIndex = new AtomicInteger();
    DoubleAdder adder = new DoubleAdder();
    double[] probabilities = vectorOdometer.getProbabilities();
    iterateOuter(
        () -> {
          iterateInner((o, i) -> adder.add(probabilities[i]));
          transferArray[tIndex.getAndAdd(1)] = adder.sumThenReset();
        });
  }
}
