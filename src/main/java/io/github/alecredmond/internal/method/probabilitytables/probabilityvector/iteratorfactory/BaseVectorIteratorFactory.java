package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public abstract class BaseVectorIteratorFactory<T extends VectorIterator>
    implements VectorIteratorFactory<T> {
  protected VectorOdometer vectorOdometer;
  protected ProbabilityVector vector;

  protected BaseVectorIteratorFactory() {}

  public T buildIterator(ProbabilityVector vector) {
    this.vector = vector;
    this.vectorOdometer = VectorIteratorFactory.blankOdometer(vector);
    fillOdometer();
    return supplyIterator().get();
  }

  protected void fillOdometer() {
    Node[] nodes = vectorOdometer.getNodeArray();
    initializeStates(nodes);
    lockPositions(nodes);
    initStateIsEventMap(nodes);
  }

  protected abstract Supplier<T> supplyIterator();

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

  protected abstract Function<Node, NodeState> initialStatePositionSetter();

  protected abstract Predicate<Node> checkLockOuter();

  protected abstract Predicate<Node> checkLockInner();

  protected abstract Function<Node, boolean[]> checkStateIsEvidence();
}
