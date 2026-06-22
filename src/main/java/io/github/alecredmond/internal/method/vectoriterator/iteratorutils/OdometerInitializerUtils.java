package io.github.alecredmond.internal.method.vectoriterator.iteratorutils;

import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import lombok.Data;

@Data
public class OdometerInitializerUtils {

  private OdometerInitializerUtils() {}

  public static void resetInnerInitializer(VectorOdometer odometer, OdometerInitializer initInner) {
    resetInitializer(odometer, odometer.getInnerIteratorLocks(), initInner);
  }

  public static void resetInitializer(
      VectorOdometer odometer, boolean[] positionLocked, OdometerInitializer initializer) {
    int fastestPos = findFastestPosition(positionLocked);
    boolean fireOnlyOnce = fastestPos < 0;
    int[] strideLengths = odometer.getStrideLengths();
    int startIndex = computeStartIndex(odometer.getStateIndexes(), strideLengths);
    int baseStride = fireOnlyOnce ? 0 : strideLengths[fastestPos];

    initializer.setLockedPositions(positionLocked);
    initializer.setInitialIndex(startIndex);
    initializer.setFastestPosition(fastestPos);
    initializer.setFireOnlyOnce(fireOnlyOnce);
    initializer.setBaseStride(baseStride);
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

  public static int[] buildStrideIfLocked(VectorOdometer odometer) {
    int[] numberOfStates = odometer.getNumberOfStates();
    int[] stepMultiplier = odometer.getStrideLengths();
    int[] strideIfLocked = new int[numberOfStates.length];
    for (int i = 0; i < numberOfStates.length; i++) {
      strideIfLocked[i] = (numberOfStates[i] - 1) * stepMultiplier[i];
    }
    return strideIfLocked;
  }

  public static void updateStartIndex(
      OdometerInitializer initializer, VectorOdometer odometer) {
    initializer.setInitialIndex(
        computeStartIndex(odometer.getStateIndexes(), odometer.getStrideLengths()));
  }

  public static void resetOuterInitializer(VectorOdometer odometer, OdometerInitializer initOuter) {
    resetInitializer(odometer, odometer.getOuterIteratorLocks(), initOuter);
  }
}
