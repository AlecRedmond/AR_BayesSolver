package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerResetLogic;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Function;
import java.util.function.Predicate;

public class JunctionTableSummer implements OdometerResetLogic {
  private final BaseVectorIterator iterator;
  private final JunctionTreeTable table;
  private Map<Node, NodeState> request;

  public JunctionTableSummer(JunctionTreeTable table) {
    this.table = table;
    this.request = new HashMap<>();
    this.iterator = new BaseVectorIterator(table.getVector(), this);
  }

  public double sum(Map<Node, NodeState> request) {
    this.request = request;
    iterator.reset();
    DoubleAdder adder = new DoubleAdder();
    double[] p = table.getVector().getProbabilities();
    iterator.iterateInner((o, i) -> adder.add(p[i]));
    return adder.sum();
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return node -> request.containsKey(node) ? request.get(node) : node.getNodeStates().getFirst();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> false;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return request::containsKey;
  }
}
