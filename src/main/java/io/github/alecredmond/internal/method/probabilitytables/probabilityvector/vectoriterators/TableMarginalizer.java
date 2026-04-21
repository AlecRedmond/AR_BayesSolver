package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory.TableMarginalizerFactory;
import java.util.concurrent.atomic.DoubleAdder;

public class TableMarginalizer extends BaseVectorIterator implements VectorIterator {

  public TableMarginalizer(VectorOdometer vectorOdometer) {
    super(vectorOdometer);
  }

  public static TableMarginalizer build(ProbabilityTable table) {
    return new TableMarginalizerFactory().build(table);
  }

  @Override
  public void performRun() {
    double[] probabilities = vectorOdometer.getProbabilities();
    DoubleAdder adder = new DoubleAdder();

    iterateOuter(
        () -> {
          iterateInner((o, i) -> adder.add(probabilities[i]));
          double sum = adder.sumThenReset();
          double ratio = sum == 0.0 ? 0.0 : 1.0 / sum;
          iterateInner((o, i) -> probabilities[i] = probabilities[i] * ratio);
        });
  }
}
