package io.github.alecredmond.method.probabilitytables.probabilityvector;

import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.probabilityvector.VectorCombinationKey;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProbabilityVectorIterator {

  public void iterateKeyCombos(
      Collection<NodeState> request,
      ProbabilityTable table,
      ObjIntConsumer<int[]> iterativeConsumer) {
    VectorCombinationKey key = new VectorCombinationKeyFactory().buildKey(table, request);
    iterateKeyCombos(table.getVector(), key, iterativeConsumer);
  }

  public void iterateKeyCombos(
      ProbabilityVector vector, VectorCombinationKey key, ObjIntConsumer<int[]> iterativeConsumer) {
    iterateKeyCombos(vector, key.getTumblerKey(), key.getPositionLocked(), iterativeConsumer);
  }

  public void iterateKeyCombos(
      ProbabilityVector vector,
      int[] tumblerKey,
      boolean[] positionLocked,
      ObjIntConsumer<int[]> iterativeConsumer) {
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

  public void adjustToRatio(
      ProbabilityVector vector,
      VectorCombinationKey comboKey,
      double ratioIfRequest,
      double ratioElsewhere) {
    int[] requestKey = comboKey.getTumblerKey();
    boolean[] positionLocked = comboKey.getPositionLocked();

    List<Integer> requestLockedPositions =
        IntStream.range(0, requestKey.length).filter(i -> positionLocked[i]).boxed().toList();

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

    iterateKeyCombos(vector, comboKey, consumer);
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
