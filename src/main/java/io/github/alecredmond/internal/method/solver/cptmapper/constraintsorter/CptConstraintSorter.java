package io.github.alecredmond.internal.method.solver.cptmapper.constraintsorter;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityVector;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class CptConstraintSorter<P extends ProbabilityConstraint, T extends NetworkTable> {
  private final Function<ProbabilityConstraint, P> conversionFunc;
  private final Predicate<ProbabilityConstraint> instanceOfP;
  private final BiPredicate<P, T> constraintIsCptEntry;
  private final T networkTable;

  protected CptConstraintSorter(T networkTable) {
    this.networkTable = networkTable;
    this.conversionFunc = buildConversionFunc();
    this.instanceOfP = buildConstraintInstanceChecker();
    this.constraintIsCptEntry = buildCptEntryChecker();
  }

  protected abstract Function<ProbabilityConstraint, P> buildConversionFunc();

  protected abstract Predicate<ProbabilityConstraint> buildConstraintInstanceChecker();

  protected abstract BiPredicate<P, T> buildCptEntryChecker();

  public List<P> sortConstraints(Collection<ProbabilityConstraint> constraints) {
    List<P> cptEntryConstraints =
        constraints.stream()
            .filter(instanceOfP)
            .map(conversionFunc)
            .filter(p -> constraintIsCptEntry.test(p, networkTable))
            .toList();
    return radixSortConstraints(cptEntryConstraints);
  }

  private List<P> radixSortConstraints(Collection<P> constraints) {
    ProbabilityVector vector = networkTable.getVector();
    Map<NodeState, Integer> stateValueMap = vector.getStateValueMap();
    Comparator<P> comparator =
        Arrays.stream(vector.getNodeArray())
            .map(node -> compareByStateIndexInNode(node, stateValueMap))
            .reduce(Comparator::thenComparing)
            .orElseThrow();
    return constraints.stream().distinct().sorted(comparator).toList();
  }

  private Comparator<P> compareByStateIndexInNode(
      Node node, Map<NodeState, Integer> stateValueMap) {
    return Comparator.comparingInt(
        (P constraint) ->
            constraint.getAllStates().stream()
                .filter(state -> state.getNode().equals(node))
                .findAny()
                .map(stateValueMap::get)
                .orElseThrow());
  }
}
