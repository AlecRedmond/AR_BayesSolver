package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import static io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator.UPDATE_STATES;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerResetLogic;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StateCombinationGenerator implements OdometerResetLogic {
  private final VectorIterator iterator;
  private final ProbabilityVector vector;
  private int[] includedPositions;
  private Set<Node> includedNodes;

  public StateCombinationGenerator(ProbabilityTable table) {
    this.vector = table.getVector();
    this.includedNodes = new HashSet<>();
    this.iterator = new VectorIterator(table.getVector(), this, UPDATE_STATES);
  }

  public <T extends Collection<NodeState>, R extends T> List<T> generateCombos(
      Set<Node> includedNodes, Supplier<R> supplier) {
    this.includedNodes = includedNodes;
    iterator.reset();
    NodeState[] states = iterator.getVectorOdometer().getStates();
    List<T> stateCombinations = new ArrayList<>();
    iterator.iterateInner(
        (o, i) ->
            stateCombinations.add(
                Arrays.stream(includedPositions)
                    .mapToObj(x -> states[x])
                    .collect(Collectors.toCollection(supplier))));
    return stateCombinations;
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> false;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return node -> !includedNodes.contains(node);
  }

  @Override
  public Runnable postUpdateLogic() {
    return () -> includedPositions = buildIncludedPositions(includedNodes);
  }

  private int[] buildIncludedPositions(Set<Node> includedNodes) {
    Node[] nodeArray = vector.getNodeArray();
    return IntStream.range(0, nodeArray.length)
        .filter(x -> includedNodes.contains(nodeArray[x]))
        .toArray();
  }
}
