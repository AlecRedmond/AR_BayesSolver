package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerResetLogic;
import java.util.Set;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Predicate;

public class TableMarginalizer implements OdometerResetLogic {
  private final ProbabilityTable table;
  private final VectorIterator iterator;

  public TableMarginalizer(ProbabilityTable table) {
    this.table = table;
    this.iterator = new VectorIterator(table.getVector(), this);
  }

  public void marginalize() {
    double[] probabilities = iterator.getVectorOdometer().getProbabilities();
    DoubleAdder adder = new DoubleAdder();
    iterator.iterateOuter(
        () -> {
          iterator.iterateInner((o, i) -> adder.add(probabilities[i]));
          double sum = adder.sumThenReset();
          double ratio = sum == 0.0 ? 0.0 : 1.0 / sum;
          iterator.iterateInner((o, i) -> probabilities[i] = probabilities[i] * ratio);
        });
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    Set<Node> events = table.getEvents();
    return events::contains;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    Set<Node> conditions = table.getConditions();
    return conditions::contains;
  }
}
