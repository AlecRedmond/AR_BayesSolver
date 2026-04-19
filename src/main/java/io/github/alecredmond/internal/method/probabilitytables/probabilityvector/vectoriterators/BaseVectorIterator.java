package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.OdometerInitializer;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerInitializerBuilder;
import java.util.function.ObjIntConsumer;

public abstract class BaseVectorIterator {
  protected static final UpdateConsumer BLANK_CONSUMER = (currentIndex, overflow, odometer) -> {};
  protected VectorOdometer vectorOdometer;

  protected BaseVectorIterator(VectorOdometer vectorOdometer) {
    this.vectorOdometer = vectorOdometer;
  }

  protected void iterateEvents(ObjIntConsumer<VectorOdometer> indexConsumer) {
    iterateEvents(indexConsumer, BLANK_CONSUMER);
  }

  protected void iterateEvents(
      ObjIntConsumer<VectorOdometer> indexConsumer, UpdateConsumer updateConsumer) {
    iterate(
        vectorOdometer,
        indexConsumer,
        updateConsumer,
        OdometerInitializerBuilder.initIterateEvents(vectorOdometer));
  }

  protected void iterate(
      VectorOdometer odometer,
      ObjIntConsumer<VectorOdometer> indexConsumer,
      UpdateConsumer updateConsumer,
      OdometerInitializer tracker) {
    int currentIndex = tracker.getInitialIndex();

    if (tracker.isFireOnlyOnce()) {
      indexConsumer.accept(odometer, currentIndex);
      return;
    }

    int fastestPosition = tracker.getFastestPosition();
    int baseStride = tracker.getBaseStride();
    int[] numberOfStates = odometer.getNumberOfStates();
    int[] lockedPositionCorrections = tracker.getLockedPositionIndexStrides();
    int[] odometerValues = odometer.getOdometerValues();
    boolean[] positionLocked = tracker.getLockedPositions();
    boolean overflow = false;

    while (!overflow) {
      indexConsumer.accept(odometer, currentIndex);
      currentIndex += baseStride;
      for (int position = fastestPosition; position >= 0; position--) {
        if (positionLocked[position]) {
          currentIndex += lockedPositionCorrections[position];
          continue;
        }
        odometerValues[position] = (odometerValues[position] + 1) % numberOfStates[position];
        overflow = odometerValues[position] == 0;
        updateConsumer.update(currentIndex, overflow, odometer);
        if (!overflow) {
          break;
        }
      }
    }
  }

  protected void iterateConditions(ObjIntConsumer<VectorOdometer> indexConsumer) {
    iterateConditions(indexConsumer, BLANK_CONSUMER);
  }

  protected void iterateConditions(
      ObjIntConsumer<VectorOdometer> indexConsumer, UpdateConsumer updateConsumer) {
    iterate(
        vectorOdometer,
        indexConsumer,
        updateConsumer,
        OdometerInitializerBuilder.initIterateConditions(vectorOdometer));
  }

  @FunctionalInterface
  protected interface UpdateConsumer {
    void update(int currentIndex, boolean overflow, VectorOdometer odometer);
  }
}
