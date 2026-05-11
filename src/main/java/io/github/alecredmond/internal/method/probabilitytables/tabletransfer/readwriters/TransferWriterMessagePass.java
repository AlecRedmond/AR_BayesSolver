package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory.TransferWriterMessagePassFactory;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class TransferWriterMessagePass extends VectorIterator<VectorOdometer> implements TransferIterator {
  private final double[] transferArray;
  private final double[] ratioArray;
  private final double[] separatorProbs;

  public TransferWriterMessagePass(
      ProbabilityVector write,
      double[] transferArray,
      TransferWriterMessagePassFactory logic,
      ProbabilityVector separatorVector) {
    super(write, logic, VectorOdometer::new);
    this.transferArray = transferArray;
    this.ratioArray = new double[transferArray.length];
    this.separatorProbs = separatorVector.getProbabilities();
  }

  @Override
  public void performRun() {
    fillRatioArray();
    AtomicInteger tIndex = new AtomicInteger();
    double[] probabilities = controller.getOdometer().getProbabilities();
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
