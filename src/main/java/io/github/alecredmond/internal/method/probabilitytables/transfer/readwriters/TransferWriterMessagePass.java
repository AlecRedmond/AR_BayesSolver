package io.github.alecredmond.internal.method.probabilitytables.transfer.readwriters;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerResetLogic;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.BaseVectorIterator;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class TransferWriterMessagePass extends BaseVectorIterator implements VectorIterator {
  private final double[] transferArray;
  private final double[] ratioArray;
  private final double[] separatorProbs;

  public TransferWriterMessagePass(
      ProbabilityVector write,
      double[] transferArray,
      OdometerResetLogic logic,
      ProbabilityVector separatorVector) {
    super(write, logic);
    this.transferArray = transferArray;
    this.ratioArray = new double[transferArray.length];
    this.separatorProbs = separatorVector.getProbabilities();
  }

  @Override
  public void performRun() {
    fillRatioArray();
    AtomicInteger tIndex = new AtomicInteger();
    double[] probabilities = vectorOdometer.getProbabilities();
    iterateOuter(
        (() -> {
          double ratio = ratioArray[tIndex.getAndAdd(1)];
          iterateInner((o, i) -> probabilities[i] = probabilities[i] * ratio);
        }));
    setNewSeparators();
  }

  private void fillRatioArray() {
    IntStream.range(0, separatorProbs.length)
        .forEach(i -> ratioArray[i] = ratioOrZero(transferArray[i], separatorProbs[i]));
  }

  private void setNewSeparators() {
    System.arraycopy(transferArray, 0, separatorProbs, 0, separatorProbs.length);
  }

  private double ratioOrZero(double numerator, double divisor) {
    return divisor == 0.0 ? 0.0 : numerator / divisor;
  }
}
