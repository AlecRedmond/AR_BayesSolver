package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.TableMarginalizer;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TableMarginalizerFactory extends BaseVectorIteratorFactory<TableMarginalizer> {
  private ProbabilityTable table;

  public TableMarginalizerFactory() {
    super();
  }

  public TableMarginalizer build(ProbabilityTable table) {
    this.table = table;
    return buildIterator(table.getVector());
  }

  @Override
  protected Supplier<TableMarginalizer> supplyIterator() {
    return () -> new TableMarginalizer(vectorOdometer);
  }

  @Override
  protected Function<Node, NodeState> initialStatePositionSetter() {
    return node -> node.getNodeStates().getFirst();
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

  @Override
  protected Function<Node, boolean[]> checkStateIsEvidence() {
    return node -> new boolean[0];
  }
}
