package io.github.alecredmond.method.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;

@Getter
public class ProbabilityVectorUtils {
  private final ProbabilityVector vector;

  public ProbabilityVectorUtils(ProbabilityVector vector) {
    this.vector = vector;
  }

  public double sumProbabilitiesWithStates(Map<Node, NodeState> request) {
    double[] probability = vector.getProbabilities();
    DoubleAdder adder = new DoubleAdder();
    consumeFromRequest(request, (key, index) -> adder.add(probability[index]));
    return adder.doubleValue();
  }

  private void consumeFromRequest(
      Map<Node, NodeState> request, BiConsumer<int[], Integer> iterativeConsumer) {
    int keyLength = vector.getNodes().length;
    int[] tumblerKey = new int[keyLength];
    boolean[] positionLocked = new boolean[keyLength];
    request.forEach(
        (node, state) -> {
          int nodeIndex = vector.getNodeIndexMap().get(node);
          int stateValue = vector.getStateValueMap().get(state);
          tumblerKey[nodeIndex] = stateValue;
          positionLocked[nodeIndex] = true;
        });
    iterateAllKeysAndApply(tumblerKey, positionLocked, iterativeConsumer);
  }

  private void iterateAllKeysAndApply(
      int[] tumblerKey, boolean[] positionLocked, BiConsumer<int[], Integer> iterativeConsumer) {
    int[] stepMultiplier = vector.getStepMultiplier();

    int currentIndex =
        IntStream.range(0, tumblerKey.length).map(i -> tumblerKey[i] * stepMultiplier[i]).sum();

    List<Integer> movablePositions =
        IntStream.range(0, tumblerKey.length).filter(i -> !positionLocked[i]).boxed().toList();

    if (movablePositions.isEmpty()) {
      iterativeConsumer.accept(tumblerKey, currentIndex);
      return;
    }

    int fastestIteratingPos = movablePositions.getLast();
    int baseStride = stepMultiplier[fastestIteratingPos];

    int[] numberOfStates = vector.getNumberOfStates();

    int[] lockedValueStrides = new int[tumblerKey.length];
    IntStream.range(0, tumblerKey.length)
        .filter(i -> positionLocked[i])
        .forEach(i -> lockedValueStrides[i] = (numberOfStates[i] - 1) * stepMultiplier[i]);

    boolean overflow = false;
    while (!overflow) {
      iterativeConsumer.accept(tumblerKey, currentIndex);
      overflow = true;
      currentIndex += baseStride;
      for (int position = fastestIteratingPos; position >= 0; position--) {
        if (positionLocked[position]) {
          currentIndex += lockedValueStrides[position];
          continue;
        }
        tumblerKey[position] = (tumblerKey[position] + 1) % numberOfStates[position];
        overflow = tumblerKey[position] == 0;
        if (!overflow) {
          break;
        }
      }
    }
  }

  public List<Integer> collectIndexesWithStates(Map<Node, NodeState> request) {
    List<Integer> indexes = new ArrayList<>();
    consumeFromRequest(request, (key, index) -> indexes.add(index));
    return indexes;
  }

  public List<Set<NodeState>> generateStateCombinations(Map<Node, NodeState> lockedStates) {
    List<Set<NodeState>> combos = new ArrayList<>();
    Node[] nodes = vector.getNodes();

    consumeFromRequest(
        lockedStates,
        (key, index) ->
            combos.add(
                IntStream.range(0, key.length)
                    .mapToObj(i -> nodes[i].getNodeStates().get(key[i]))
                    .collect(Collectors.toCollection(LinkedHashSet::new))));

    return combos;
  }

  public void marginalizeVector(Set<Node> conditions) {
    int keyLength = vector.getNodes().length;
    int[] key = new int[keyLength];

    boolean[] conditionLocks = new boolean[keyLength];
    boolean[] eventLocks = new boolean[keyLength];

    conditions.forEach(condition -> conditionLocks[vector.getNodeIndexMap().get(condition)] = true);
    IntStream.range(0, eventLocks.length).forEach(i -> eventLocks[i] = !conditionLocks[i]);

    double[] probs = vector.getProbabilities();

    iterateAllKeysAndApply(
        key,
        eventLocks,
        (outerKey, outerIndex) -> {
          DoubleAdder adder = new DoubleAdder();
          iterateAllKeysAndApply(
              outerKey, conditionLocks, (innerKey, innerIndex) -> adder.add(probs[innerIndex]));
          double sum = adder.sum();
          if (sum == 0) return;
          double ratio = 1 / sum;
          iterateAllKeysAndApply(
              outerKey,
              conditionLocks,
              (innerKey, innerIndex) -> probs[innerIndex] = ratio * probs[innerIndex]);
        });
  }
}
