package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.BaseVectorIterator;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.BaseVectorIterator.UpdateConsumer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public abstract class BaseVectorIteratorFactory<T extends VectorIterator>
    implements VectorIteratorFactory<T> {
  protected static final UpdateConsumer UPDATE_STATES =
      (currentIndex, overflow, odometer) -> {
        NodeState[][] stateArrays = odometer.getStateArrays();
        NodeState[] states = odometer.getStates();
        int[] stateIndexes = odometer.getStateIndexes();
        IntStream.range(0, states.length)
            .forEach(
                x -> {
                  int y = stateIndexes[x];
                  states[x] = stateArrays[x][y];
                });
      };

  protected VectorOdometer vectorOdometer;
  protected ProbabilityVector vector;

  protected BaseVectorIteratorFactory() {}

  public T buildIterator(ProbabilityVector vector) {
    this.vector = vector;
    this.vectorOdometer = VectorIteratorFactory.blankOdometer(vector);
    fillOdometer();
    T t = supplyIterator();
    if (t instanceof BaseVectorIterator b) {
      b.setConsumer(updateConsumer());
    }
    return t;
  }

  protected void fillOdometer() {
    Node[] nodes = vectorOdometer.getNodeArray();
    initializeStates(nodes);
    lockPositions(nodes);
    initStateIsEventMap(nodes);
  }

  protected abstract T supplyIterator();

  protected UpdateConsumer updateConsumer() {
    return (currentIndex, overflow, odometer) -> {};
  }

  protected void initializeStates(Node[] nodeArray) {
    int[] stateIndexes = vectorOdometer.getStateIndexes();
    NodeState[] states = vectorOdometer.getStates();
    Map<NodeState, Integer> stateValueMap = vector.getStateValueMap();
    Function<Node, NodeState> initialStateMapper = initialStatePositionSetter();
    IntStream.range(0, nodeArray.length)
        .forEach(
            i -> {
              Node node = nodeArray[i];
              NodeState state = initialStateMapper.apply(node);
              states[i] = state;
              stateIndexes[i] = stateValueMap.get(state);
            });
  }

  protected void lockPositions(Node[] nodes) {
    boolean[] outerIteratorLocks = vectorOdometer.getOuterIteratorLocks();
    boolean[] innerIteratorLocks = vectorOdometer.getInnerIteratorLocks();
    Predicate<Node> outerFn = checkLockOuter();
    Predicate<Node> innerFn = checkLockInner();
    IntStream.range(0, nodes.length)
        .forEach(
            x -> {
              Node node = nodes[x];
              outerIteratorLocks[x] = outerFn.test(node);
              innerIteratorLocks[x] = innerFn.test(node);
            });
  }

  protected void initStateIsEventMap(Node[] nodes) {
    boolean[][] isEvidenceArray = vectorOdometer.getNodeStateEvidenceArray();
    Function<Node, boolean[]> function = checkStateIsEvidence();
    IntStream.range(0, nodes.length).forEach(x -> isEvidenceArray[x] = function.apply(nodes[x]));
  }

  protected Function<Node, NodeState> initialStatePositionSetter() {
    return node -> node.getNodeStates().getFirst();
  }

  protected abstract Predicate<Node> checkLockOuter();

  protected abstract Predicate<Node> checkLockInner();

  protected Function<Node, boolean[]> checkStateIsEvidence() {
    return node -> new boolean[0];
  }
}
