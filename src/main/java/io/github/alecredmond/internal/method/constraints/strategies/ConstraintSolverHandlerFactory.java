package io.github.alecredmond.internal.method.constraints.strategies;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTATableHandler;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorfactory.BaseVectorIteratorFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class ConstraintSolverHandlerFactory<
        T extends ProbabilityConstraint, R extends ConstraintSolverHandler<T>>
    extends BaseVectorIteratorFactory<R> {
  protected T constraint;
  protected JTATableHandler tableHandler;
  protected SolverHandlerConstructor<T, R> constructor;

  public ConstraintSolverHandlerFactory(
      JTATableHandler tableHandler, T constraint, SolverHandlerConstructor<T, R> constructor) {
    this.constraint = constraint;
    this.tableHandler = tableHandler;
    this.constructor = constructor;
  }

  public R build() {
    return buildIterator(tableHandler.getVector());
  }

  @Override
  protected R supplyIterator() {
    return constructor.construct(tableHandler, constraint, vectorOdometer);
  }

  @Override
  protected Function<Node, NodeState> initialStatePositionSetter() {
    Map<Node, NodeState> condMap = NodeUtils.generateRequest(constraint.getConditionStates());
    return node -> condMap.containsKey(node) ? condMap.get(node) : node.getNodeStates().getFirst();
  }

  /**
   * For constraint handlers, the outer iterator should ONLY cycle the event state(s), meaning all
   * values except the event states will be true.
   */
  @Override
  protected Predicate<Node> checkLockOuter() {
    Set<Node> events = constraint.getEventNodes();
    return node -> !events.contains(node);
  }

  /**
   * The inner iterator will cycle values not within any of the constraint states, meaning all
   * states in the constraint will be true.
   */
  @Override
  protected Predicate<Node> checkLockInner() {
    Set<Node> allNodes = constraint.getAllNodes();
    return allNodes::contains;
  }

  @Override
  protected Function<Node, boolean[]> checkStateIsEvidence() {
    Set<Node> eventNodes = constraint.getEventNodes();
    Set<NodeState> eventStates = constraint.getEventStates();
    return node -> {
      if (!eventNodes.contains(node)) {
        return new boolean[0];
      }
      List<NodeState> states = node.getNodeStates();
      boolean[] isEvidence = new boolean[states.size()];
      IntStream.range(0, states.size())
          .filter(y -> eventStates.contains(states.get(y)))
          .forEach(y -> isEvidence[y] = true);
      return isEvidence;
      // Java pls give BooleanStream functions T^T
    };
  }

  @FunctionalInterface
  public interface SolverHandlerConstructor<
      T extends ProbabilityConstraint, R extends ConstraintSolverHandler<T>> {
    R construct(
        JTATableHandler tableHandler,
        ProbabilityConstraint constraint,
        VectorOdometer vectorOdometer);
  }
}
