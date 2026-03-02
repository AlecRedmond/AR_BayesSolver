package io.github.alecredmond.method.probabilitytables.probabilityvector;

import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.probabilityvector.VectorCombinationKey;
import java.util.*;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProbabilityVectorIterator {

  public void iterateKeyCombos(
      Collection<NodeState> lockedStates,
      ProbabilityTable table,
      ObjIntConsumer<int[]> iterativeConsumer) {
    VectorCombinationKey key = new VectorCombinationKeyFactory().buildKey(table, lockedStates);
    iterateKeyCombos(table.getVector(), key, iterativeConsumer);
  }

  public void iterateKeyCombos(
      ProbabilityVector vector, VectorCombinationKey key, ObjIntConsumer<int[]> iterativeConsumer) {
    iterateKeyCombos(vector, key.getStateKey(), key.getInnerLock(), iterativeConsumer);
  }

  /**
   * This is a method where both the State Key (representing a combination of NodeStates by their
   * indexes in their parent Node's state list) and the index (the position in the vector's
   * probability array of the combination) can be iterated through and processed sequentially with a
   * BiConsumer, while locking specific NodeState values in place.
   *
   * <p>It achieves this by advancing the tumbler key like an odometer, starting from the fastest
   * unlocked position and carrying left for every overflow encountered. An overflow that carries to
   * the leftmost position represents an overflow/reset of the tumbler key and therefore the end of
   * the iterator cycle.
   *
   * <p>Simultaneously the index is incremented by the size of the base stride (1 if the fastest
   * moving position (fp) is the rightmost, stepMultiplier[fp] otherwise). When an overflow leads
   * into a locked position (lp), the index is increased by
   *
   * <p><code>
   * (numberOfStates[lp] - 1) * nodeMultiplier[lp])</code>
   *
   * <p>which represents a stride over the other states in the position.
   */
  public void iterateKeyCombos(
      ProbabilityVector vector,
      int[] stateKey,
      boolean[] positionLocked,
      ObjIntConsumer<int[]> iterativeConsumer) {
    int[] stepMultiplier = vector.getStepMultiplier();
    int fastestPosition = findFastestPosition(positionLocked);
    int currentIndex = computeStartIndex(stateKey, stepMultiplier);

    if (fastestPosition < 0) {
      iterativeConsumer.accept(stateKey, currentIndex);
      return;
    }

    int baseStride = stepMultiplier[fastestPosition];
    int[] numberOfStates = vector.getNumberOfStates();
    int[] lockedPositionIndexCorrections =
        computeIndexCorrections(positionLocked, numberOfStates, stepMultiplier);

    boolean overflow = false;
    while (!overflow) {
      iterativeConsumer.accept(stateKey, currentIndex);
      currentIndex += baseStride;
      for (int position = fastestPosition; position >= 0; position--) {
        if (positionLocked[position]) {
          currentIndex += lockedPositionIndexCorrections[position];
          continue;
        }
        stateKey[position] = (stateKey[position] + 1) % numberOfStates[position];
        overflow = stateKey[position] == 0;
        if (!overflow) {
          break;
        }
      }
    }
  }

  private int findFastestPosition(boolean[] positionLocked) {
    int fastestPosition = -1;
    for (int i = positionLocked.length - 1; i >= 0; i--) {
      if (!positionLocked[i]) {
        fastestPosition = i;
        break;
      }
    }
    return fastestPosition;
  }

  private int computeStartIndex(int[] stateKey, int[] stepMultiplier) {
    return IntStream.range(0, stateKey.length).map(i -> stateKey[i] * stepMultiplier[i]).sum();
  }

  private int[] computeIndexCorrections(
      boolean[] positionLocked, int[] numberOfStates, int[] stepMultiplier) {
    int[] corrections = new int[positionLocked.length];
    IntStream.range(0, positionLocked.length)
        .filter(i -> positionLocked[i])
        .forEach(i -> corrections[i] = (numberOfStates[i] - 1) * stepMultiplier[i]);
    return corrections;
  }
}
