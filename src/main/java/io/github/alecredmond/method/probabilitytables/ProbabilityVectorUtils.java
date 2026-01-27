package io.github.alecredmond.method.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import lombok.Getter;

@Getter
public class ProbabilityVectorUtils {
  private final ProbabilityVector vector;

  public ProbabilityVectorUtils(ProbabilityVector vector) {
    this.vector = vector;
  }

  public double sumProbabilitiesWithStates(Map<Node, NodeState> request) {
    int keyLength = vector.getNodes().length;
    int[] lockedValues = new int[keyLength];
    boolean[] indexLocked = new boolean[keyLength];
    buildLockedValueArrays(request, lockedValues, indexLocked);

    double[] probability = vector.getProbability();
    DoubleAdder adder = new DoubleAdder();
    BiConsumer<int[], Integer> keyConsumer = (key, index) -> adder.add(probability[index]);

    incrementAndExecute(lockedValues, indexLocked, keyConsumer);

    return adder.doubleValue();
  }

  private void buildLockedValueArrays(
      Map<Node, NodeState> request, int[] lockedValues, boolean[] indexLocked) {
    Arrays.fill(lockedValues, 0);
    Arrays.fill(indexLocked, false);
    request.forEach(
        (node, state) -> {
          int nodeIndex = vector.getNodeIndexMap().get(node);
          int stateValue = vector.getStateValueMap().get(state);
          lockedValues[nodeIndex] = stateValue;
          indexLocked[nodeIndex] = true;
        });
  }

  private void incrementAndExecute(
      int[] key, boolean[] indexLocked, BiConsumer<int[], Integer> keyIndexConsumer) {
    int[] multiplier = vector.getMultiplier();
    int keyLength = key.length;
    int currentIndex = keyToIndex(key, multiplier);

    List<Integer> positionsToIterate =
        IntStream.range(0, keyLength).filter(i -> !indexLocked[i]).boxed().toList();

    if (positionsToIterate.isEmpty()) {
      keyIndexConsumer.accept(key, currentIndex);
      return;
    }

    int fastestIteratingPos = positionsToIterate.getLast();
    int baseStride = multiplier[fastestIteratingPos];

    int[] cardinality = vector.getCardinality();

    int[] skipStrides = new int[keyLength];
    IntStream.range(0, keyLength)
        .filter(i -> indexLocked[i])
        .forEach(i -> skipStrides[i] = (cardinality[i] - 1) * multiplier[i]); // TODO - CHECK

    boolean overflow = false;
    while (!overflow) {
      keyIndexConsumer.accept(key, currentIndex);
      overflow = true;
      currentIndex += baseStride;
      for (int position = fastestIteratingPos; position >= 0; position--) {
        if (indexLocked[position]) {
          currentIndex += skipStrides[position];
          continue;
        }
        key[position] = (key[position] + 1) % cardinality[position];
        overflow = key[position] == 0;
        if (!overflow) {
          break;
        }
      }
    }
  }

  private int keyToIndex(int[] key, int[] multiplier) {
    int sum = 0;
    for (int i = 0; i < key.length; i++) {
      sum += key[i] * multiplier[i];
    }
    return sum;
  }

  public List<Integer> collectIndexesWithStates(Map<Node, NodeState> request) {
    int keyLength = vector.getNodes().length;
    int[] lockedValues = new int[keyLength];
    boolean[] indexLocked = new boolean[keyLength];
    buildLockedValueArrays(request, lockedValues, indexLocked);

    List<Integer> indexes = new ArrayList<>();
    BiConsumer<int[], Integer> keyConsumer = (key, index) -> indexes.add(index);

    incrementAndExecute(lockedValues, indexLocked, keyConsumer);

    return indexes;
  }

  public void consumeFromRequest(
      Map<Node, NodeState> request, BiConsumer<int[], Integer> keyIndexConsumer) {
    int keyLength = vector.getNodes().length;
    int[] key = new int[keyLength];
    boolean[] indexLocked = new boolean[keyLength];
    buildLockedValueArrays(request, key, indexLocked);
    incrementAndExecute(key, indexLocked, keyIndexConsumer);
  }

  public List<Set<NodeState>> generateStateCombinations(Map<Node, NodeState> lockedStates) {
    int keyLength = vector.getNodes().length;
    int[] lockedValues = new int[keyLength];
    boolean[] indexLocked = new boolean[keyLength];
    buildLockedValueArrays(lockedStates, lockedValues, indexLocked);

    List<Set<NodeState>> combos = new ArrayList<>();
    Node[] nodes = vector.getNodes();
    BiConsumer<int[], Integer> keyConsumer =
        (key, index) -> {
          Set<NodeState> states = new LinkedHashSet<>();
          for (int i = 0; i < key.length; i++) {
            states.add(nodes[i].getNodeStates().get(key[i]));
          }
          combos.add(states);
        };

    incrementAndExecute(lockedValues, indexLocked, keyConsumer);

    return combos;
  }
}
