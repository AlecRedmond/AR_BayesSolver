package io.github.alecredmond.method.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.BiPredicate;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProbabilityVectorUtils {
  private ProbabilityVector vector;

  public ProbabilityVectorUtils(ProbabilityVector vector) {
    this.vector = vector;
  }

  public double sumProbabilitiesWithStates(Map<Node, NodeState> request) {
    double[] probability = vector.getProbabilities();
    DoubleAdder adder = new DoubleAdder();
    consumeFromRequest(request, (key, index) -> adder.add(probability[index]));
    return adder.doubleValue();
  }

  public void consumeFromRequest(
      Map<Node, NodeState> request, ObjIntConsumer<int[]> iterativeConsumer) {
    int keyLength = vector.getNodes().length;
    int[] tumblerKey = new int[keyLength];
    boolean[] positionLocked = new boolean[keyLength];
    buildKeyFromRequest(request, tumblerKey, positionLocked);
    iterateAllKeysAndApply(tumblerKey, positionLocked, iterativeConsumer);
  }

  private void buildKeyFromRequest(
      Map<Node, NodeState> request, int[] tumblerKey, boolean[] positionLocked) {
    request.forEach(
        (node, state) -> {
          int nodeIndex = vector.getNodeIndexMap().get(node);
          int stateValue = vector.getStateValueMap().get(state);
          tumblerKey[nodeIndex] = stateValue;
          positionLocked[nodeIndex] = true;
        });
  }

  private void iterateAllKeysAndApply(
      int[] tumblerKey, boolean[] positionLocked, ObjIntConsumer<int[]> iterativeConsumer) {
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

  public Set<Set<NodeState>> generateStateCombinations(Map<Node, NodeState> lockedStates) {
    Set<Node> allNodes = Arrays.stream(vector.getNodes()).collect(Collectors.toSet());
    return generateStateCombinations(lockedStates, allNodes);
  }

  public Set<Set<NodeState>> generateStateCombinations(
      Map<Node, NodeState> lockedStates, Set<Node> includedNodes) {
    Set<Set<NodeState>> combos = new LinkedHashSet<>();
    Node[] nodes = vector.getNodes();

    // Lock excluded nodes to their first state for faster iteration
    Map<Node, NodeState> request = new HashMap<>(lockedStates);
    Arrays.stream(nodes)
        .filter(n -> !includedNodes.contains(n))
        .forEach(n -> request.put(n, n.getNodeStates().getFirst()));

    consumeFromRequest(
        request,
        (key, index) ->
            combos.add(
                IntStream.range(0, key.length)
                    .mapToObj(i -> nodes[i].getNodeStates().get(key[i]))
                    .filter(state -> includedNodes.contains(state.getNode()))
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

  public void adjustToRatio(
      Map<Node, NodeState> request, double ratioIfRequest, double ratioElsewhere) {
    int keyLength = vector.getNodes().length;
    int[] requestKey = new int[keyLength];
    boolean[] positionLocked = new boolean[keyLength];
    buildKeyFromRequest(request, requestKey, positionLocked);

    List<Integer> requestLockedPositions =
        IntStream.range(0, keyLength).filter(i -> positionLocked[i]).boxed().toList();

    double[] probabilities = vector.getProbabilities();

    ObjIntConsumer<int[]> consumer =
        branchedConsumer(
            (key, index) -> {
              for (Integer position : requestLockedPositions) {
                if (key[position] != requestKey[position]) {
                  return false;
                }
              }
              return true;
            },
            (key, index) -> probabilities[index] = probabilities[index] * ratioIfRequest,
            (key, index) -> probabilities[index] = probabilities[index] * ratioElsewhere);

    consumeFromRequest(new HashMap<>(), consumer);
  }

  private ObjIntConsumer<int[]> branchedConsumer(
      BiPredicate<int[], Integer> predicate,
      ObjIntConsumer<int[]> trueBranch,
      ObjIntConsumer<int[]> falseBranch) {
    return (key, index) -> {
      if (predicate.test(key, index)) {
        trueBranch.accept(key, index);
      } else {
        falseBranch.accept(key, index);
      }
    };
  }
}
