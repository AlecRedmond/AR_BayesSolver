package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.OdometerInitializer;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerInitializerBuilder;
import java.util.function.ObjIntConsumer;
import lombok.Setter;

public abstract class BaseVectorIterator {
  protected VectorOdometer vectorOdometer;
  @Setter protected UpdateConsumer consumer;

  protected BaseVectorIterator(VectorOdometer vectorOdometer) {
    this.vectorOdometer = vectorOdometer;
  }

  protected BaseVectorIterator(VectorOdometer vectorOdometer, UpdateConsumer consumer) {
    this.vectorOdometer = vectorOdometer;
    this.consumer = consumer;
  }

  protected void iterateInner(ObjIntConsumer<VectorOdometer> indexConsumer) {
    iterateInner(indexConsumer, consumer);
  }

  protected void iterateInner(
      ObjIntConsumer<VectorOdometer> indexConsumer, UpdateConsumer updateConsumer) {
    iterate(
        vectorOdometer,
        indexConsumer,
        updateConsumer,
        OdometerInitializerBuilder.initIterateInner(vectorOdometer));
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
    int[] strideIfLocked = tracker.getStrideIfLocked();
    int[] stateIndexes = odometer.getStateIndexes();
    boolean[] positionLocked = tracker.getLockedPositions();
    boolean overflow = false;

    while (!overflow) {
      indexConsumer.accept(odometer, currentIndex);
      currentIndex += baseStride;
      for (int position = fastestPosition; position >= 0; position--) {
        if (positionLocked[position]) {
          currentIndex += strideIfLocked[position];
          continue;
        }
        stateIndexes[position] = (stateIndexes[position] + 1) % numberOfStates[position];
        overflow = stateIndexes[position] == 0;
        updateConsumer.update(currentIndex, overflow, odometer);
        if (!overflow) {
          break;
        }
      }
    }
  }

  protected void iterateOuter(Runnable runnable) {
    iterateOuter((o, i) -> runnable.run(), consumer);
  }

  protected void iterateOuter(
      ObjIntConsumer<VectorOdometer> indexConsumer, UpdateConsumer updateConsumer) {
    iterate(
        vectorOdometer,
        indexConsumer,
        updateConsumer,
        OdometerInitializerBuilder.initIterateOuter(vectorOdometer));
  }

  @FunctionalInterface
  public interface UpdateConsumer {
    void update(int currentIndex, boolean overflow, VectorOdometer odometer);
  }
}
