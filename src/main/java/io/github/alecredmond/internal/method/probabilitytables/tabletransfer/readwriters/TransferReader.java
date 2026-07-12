package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory.TransferReaderFactory;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class TransferReader extends VectorIterator<VectorOdometer> implements TransferIterator {
  private final double[] transferArray;
  private final double[] adder = {0.0};
  private final int[] tIndex = {0};

  public TransferReader(
      ProbabilityVector read, double[] transferArray, TransferReaderFactory logic) {
    super(read, logic, VectorOdometer::new);
    this.transferArray = transferArray;
  }

  @Override
  public void performRun() {
    tIndex[0] = 0;
    double[] probabilities = controller.getOdometer().getProbabilities();
    iterateOuter(
        () -> {
          adder[0] = 0.0;
          iterateInner((o, i) -> adder[0] += probabilities[i]);
          transferArray[tIndex[0]] = adder[0];
          tIndex[0]++;
        });
  }
}
