package io.github.alecredmond.internal.method.constraints.strategies.baseobjects;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolverHandler;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory.BaseVectorIteratorFactory;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory.VectorIteratorFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public abstract class BaseConstraintSolverHandlerFactory<T extends ProbabilityConstraint>
    extends BaseVectorIteratorFactory<T> implements VectorIteratorFactory<T> {
  protected T constraint;
  protected JTATableHandler tableHandler;

  protected BaseConstraintSolverHandlerFactory(JTATableHandler tableHandler, T constraint) {
    this.constraint = constraint;
    this.tableHandler = tableHandler;
  }

  public abstract ConstraintSolverHandler<T> build();

  protected <R extends ConstraintSolverHandler<T>> R superBuild(Class<R> rClass) {
    return rClass.cast(build(tableHandler.getVector(), constraint));
  }

  protected void initializeOdometer(
      T constraint, NodeState[] statesArray, int[] stateIndexes, Node[] nodeArray) {
    Set<Node> allNodes = constraint.getAllNodes();
    Map<NodeState, Integer> nodeStateIndexMap = vector.getStateValueMap();
    IntStream.range(0, nodeArray.length)
        .forEach(
            i -> {
              Node node = nodeArray[i];
              List<NodeState> states = node.getNodeStates();
              if (!allNodes.contains(node)) {
                statesArray[i] = states.getFirst();
                return;
              }
              NodeState initialState = getInitialState(constraint, node);
              statesArray[i] = initialState;
              stateIndexes[i] = nodeStateIndexMap.get(initialState);
            });
  }

  protected NodeState getInitialState(T constraint, Node node) {
    return node.getNodeStates().stream()
        .filter(constraint.getAllStates()::contains)
        .findFirst()
        .orElseThrow();
  }

  protected void initializeEventAndConditionStates(
      T constraint,
      boolean[] eventStatePosition,
      boolean[] conditionStatePosition,
      Node[] nodeArray) {
    Set<Node> eventNodes = constraint.getEventNodes();
    Set<Node> conditionNodes = constraint.getConditionNodes();
    IntStream.range(0, nodeArray.length)
        .forEach(
            i -> {
              Node node = nodeArray[i];
              eventStatePosition[i] = eventNodes.contains(node);
              conditionStatePosition[i] = conditionNodes.contains(node);
            });
  }

  protected void initializeStateIsEvent(
      T requestItem,
      boolean[][] stateIsEvent,
      boolean[] eventStatePosition,
      NodeState[][] stateArrays) {
    Set<NodeState> eventStates = requestItem.getEventStates();
    IntStream.range(0, eventStatePosition.length)
        .filter(i -> eventStatePosition[i])
        .forEach(
            x -> {
              boolean[] isEventArray = stateIsEvent[x];
              NodeState[] states = stateArrays[x];
              IntStream.range(0, isEventArray.length)
                  .forEach(
                      y -> {
                        boolean setAsEvent = eventStates.contains(states[y]);
                        stateIsEvent[x][y] = setAsEvent;
                      });
            });
  }
}
