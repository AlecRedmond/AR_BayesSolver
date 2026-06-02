package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

import static io.github.alecredmond.internal.method.utils.DoublePrecision.fuzzyEquals;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory.TransferWriterMarginalFactory;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class TransferWriterMarginal extends VectorIterator<VectorOdometer>
    implements TransferIterator {
  private final double[] transferArray;
  private final double[] adder = {0.0};
  private final int[] tIndex = {0};

  public TransferWriterMarginal(
      ProbabilityVector write, double[] transferArray, TransferWriterMarginalFactory logic) {
    super(write, logic, VectorOdometer::new);
    this.transferArray = transferArray;
  }

  @Override
  public void performRun() {
    tIndex[0] = 0;
    double[] probabilities = controller.getOdometer().getProbabilities();
    iterateOuter(
        (() -> {
          adder[0] = 0.0;
          iterateInner((o, i) -> adder[0] += probabilities[i]);
          double actual = adder[0];
          double expected = transferArray[tIndex[0]];
          tIndex[0]++;
          if (fuzzyEquals(expected, actual)) return;
          double ratio = actual == 0.0 ? 0.0 : expected / actual;
          iterateInner((o, i) -> probabilities[i] *= ratio);
        }));
  }
}
