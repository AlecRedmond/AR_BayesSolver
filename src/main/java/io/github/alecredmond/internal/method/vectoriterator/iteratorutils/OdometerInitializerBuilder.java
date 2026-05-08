package io.github.alecredmond.internal.method.vectoriterator.iteratorutils;

import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import java.util.stream.IntStream;
import lombok.Data;

@Data
public class OdometerInitializerBuilder {

  private OdometerInitializerBuilder() {}

  public static OdometerInitializer initIterateInner(VectorOdometer odometer) {
    return initialize(odometer, odometer.getInnerIteratorLocks());
  }

  private static OdometerInitializer initialize(VectorOdometer odometer, boolean[] positionLocked) {
    OdometerInitializer tracker = new OdometerInitializer();
    tracker.setLockedPositions(positionLocked);
    int fastestPos = findFastestPosition(positionLocked);
    boolean fireOnlyOnce = fastestPos < 0;
    tracker.setFastestPosition(fastestPos);
    tracker.setFireOnlyOnce(fireOnlyOnce);
    int[] stepMultiplier = odometer.getStepMultiplier();
    tracker.setInitialIndex(computeStartIndex(odometer.getStateIndexes(), stepMultiplier));
    if (fireOnlyOnce) return tracker;
    tracker.setBaseStride(stepMultiplier[fastestPos]);
    tracker.setStrideIfLocked(
        computeIndexCorrections(positionLocked, odometer.getNumberOfStates(), stepMultiplier));
    return tracker;
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

  private static int computeStartIndex(int[] odometerValues, int[] stepMultiplier) {
    return IntStream.range(0, odometerValues.length)
        .map(i -> odometerValues[i] * stepMultiplier[i])
        .sum();
  }

  private static int[] computeIndexCorrections(
      boolean[] positionLocked, int[] numberOfStates, int[] stepMultiplier) {
    int[] corrections = new int[positionLocked.length];
    IntStream.range(0, positionLocked.length)
        .filter(i -> positionLocked[i])
        .forEach(i -> corrections[i] = (numberOfStates[i] - 1) * stepMultiplier[i]);
    return corrections;
  }

  public static OdometerInitializer initIterateOuter(VectorOdometer odometer) {
    return initialize(odometer, odometer.getOuterIteratorLocks());
  }
}
