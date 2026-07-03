package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.OdometerResetOnlyOnBuild;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.ResetLogicUtils;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.updatelogictypes.OdometerUpdateBlank;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class TableNormalizer implements OdometerResetOnlyOnBuild, OdometerUpdateBlank {
  private final ProbabilityTable table;
  private final VectorIterator<VectorOdometer> iterator;
  private final double[] adder = {0.0};

  public TableNormalizer(ProbabilityTable table) {
    this.table = table;
    this.iterator = new VectorIterator<>(table.getVector(), this, VectorOdometer::new);
  }

  public void normalize() {
    double[] probabilities = table.getProbabilities();
    iterator.iterateOuter(
        () -> {
          adder[0] = 0.0;
          iterator.iterateInner((o, i) -> adder[0] += probabilities[i]);
          double sum = adder[0];
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

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return ResetLogicUtils.initializeToFirstNodeStates();
  }
}
