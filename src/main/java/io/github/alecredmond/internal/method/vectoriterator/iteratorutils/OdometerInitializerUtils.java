package io.github.alecredmond.internal.method.vectoriterator.iteratorutils;

import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import java.util.stream.IntStream;
import lombok.Data;

@Data
public class OdometerInitializerUtils {

  private OdometerInitializerUtils() {}

  public static OdometerInitializer initIterateInner(VectorOdometer odometer) {
    return initialize(odometer, odometer.getInnerIteratorLocks());
  }

  private static OdometerInitializer initialize(VectorOdometer odometer, boolean[] positionLocked) {
    OdometerInitializer initializer = new OdometerInitializer();
    updateInitializer(odometer, positionLocked, initializer);
    return initializer;
  }

  public static void updateInitializer(
      VectorOdometer odometer, boolean[] positionLocked, OdometerInitializer initializer) {
    initializer.setLockedPositions(positionLocked);
    int fastestPos = findFastestPosition(positionLocked);
    boolean fireOnlyOnce = fastestPos < 0;
    initializer.setFastestPosition(fastestPos);
    initializer.setFireOnlyOnce(fireOnlyOnce);
    int[] strideLengths = odometer.getStrideLengths();
    initializer.setInitialIndex(computeStartIndex(odometer.getStateIndexes(), strideLengths));
    if (fireOnlyOnce) return;
    initializer.setBaseStride(strideLengths[fastestPos]);
    initializer.setStrideIfLocked(
        computeIndexCorrections(positionLocked, odometer.getNumberOfStates(), strideLengths));
  }

  private static int findFastestPosition(boolean[] positionLocked) {
    int fastestPosition = -1;
    for (int i = positionLocked.length - 1; i >= 0; i--) {
      if (!positionLocked[i]) {
        fastestPosition = i;
        break;
      }
    }
    return fastestPosition;
  }

  private static int computeStartIndex(int[] odometerValues, int[] strideLengths) {
    int index = 0;
    for (int i = 0; i < odometerValues.length; i++) {
      index += odometerValues[i] * strideLengths[i];
    }
    return index;
  }

  private static int[] computeIndexCorrections(
      boolean[] positionLocked, int[] numberOfStates, int[] stepMultiplier) {
    int[] corrections = new int[positionLocked.length];
    IntStream.range(0, positionLocked.length)
        .filter(i -> positionLocked[i])
        .forEach(i -> corrections[i] = (numberOfStates[i] - 1) * stepMultiplier[i]);
    return corrections;
  }

  public static void updateIterateInner(OdometerInitializer initializer, VectorOdometer odometer) {
    initializer.setInitialIndex(
        computeStartIndex(odometer.getStateIndexes(), odometer.getStrideLengths()));
  }

  public static OdometerInitializer initIterateOuter(VectorOdometer odometer) {
    return initialize(odometer, odometer.getOuterIteratorLocks());
  }
}
