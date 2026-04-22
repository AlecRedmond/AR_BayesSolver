package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.BaseVectorIterator;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.ConstraintBuilderIterator;
import java.util.Set;
import java.util.function.Predicate;

public class ConstraintBuilderIteratorFactory
    extends BaseVectorIteratorFactory<ConstraintBuilderIterator> {
  private ProbabilityTable table;

  public ConstraintBuilderIteratorFactory() {
    super();
  }

  public ConstraintBuilderIterator build(ProbabilityTable table) {
    this.table = table;
    return super.buildIterator(table.getVector());
  }

  @Override
  protected ConstraintBuilderIterator constructIterator() {
    return new ConstraintBuilderIterator(vectorOdometer);
  }

  @Override
  protected BaseVectorIterator.UpdateConsumer updateConsumer() {
    return UPDATE_STATES;
  }

  @Override
  protected Predicate<Node> checkLockOuter() {
    Set<Node> events = table.getEvents();
    return events::contains;
  }

  @Override
  protected Predicate<Node> checkLockInner() {
    Set<Node> conditions = table.getConditions();
    return conditions::contains;
  }
}
