package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerResetLogic;
import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class JunctionTableSummer implements OdometerResetLogic {
  private final VectorIterator iterator;
  private final JunctionTreeTable table;
  private final DoubleAdder adder;
  private Set<Node> requestNodes;
  private Set<NodeState> requestStates;

  public JunctionTableSummer(JunctionTreeTable table) {
    this.table = table;
    this.requestNodes = new HashSet<>();
    this.requestStates = new HashSet<>();
    this.iterator = new VectorIterator(table.getVector(), this);
    this.adder = new DoubleAdder();
  }

  public double sum(Collection<NodeState> states) {
    this.requestStates = new HashSet<>(states);
    this.requestNodes = NodeUtils.getNodes(states);
    iterator.reset();

    double[] p = table.getVector().getProbabilities();
    int[] stateIndexes = iterator.getVectorOdometer().getStateIndexes();
    boolean[][] stateIsEvent = iterator.getVectorOdometer().getNodeStateEvidenceArray();

    iterator.iterateOuter(
        () -> {
          if (!checkIsEvidence(stateIndexes, stateIsEvent)) return;
          iterator.iterateInner((o, i) -> adder.add(p[i]));
        });
    return adder.sumThenReset();
  }

  protected boolean checkIsEvidence(int[] stateIndexes, boolean[][] stateIsEvent) {
    return IntStream.range(0, stateIsEvent.length)
        .filter(x -> stateIsEvent[x].length != 0)
        .allMatch(x -> stateIsEvent[x][stateIndexes[x]]);
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return node ->
        requestNodes.contains(node)
            ? node.getNodeStates().stream()
                .filter(requestStates::contains)
                .findFirst()
                .orElseThrow()
            : node.getNodeStates().getFirst();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> !requestNodes.contains(node);
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return requestNodes::contains;
  }

  @Override
  public Function<Node, boolean[]> buildEvidenceMaps() {
    return node -> {
      if (!requestNodes.contains(node)) {
        return new boolean[0];
      }
      List<NodeState> states = node.getNodeStates();
      boolean[] isEvidence = new boolean[states.size()];
      IntStream.range(0, states.size())
          .filter(y -> requestStates.contains(states.get(y)))
          .forEach(y -> isEvidence[y] = true);
      return isEvidence;
    };
  }
}
