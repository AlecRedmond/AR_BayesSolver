package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.OdometerInitializer;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerInitializerBuilder;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;

public abstract class BaseVectorIterator {
  protected static final UpdateConsumer BLANK_CONSUMER = (currentIndex, overflow, odometer) -> {};
  protected static final UpdateConsumer UPDATE_CONSUMER =
      (currentIndex, overflow, odometer) -> {
        NodeState[][] stateArrays = odometer.getStateArrays();
        NodeState[] states = odometer.getStates();
        int[] stateIndexes = odometer.getStateIndexes();
        IntStream.range(0, states.length)
            .forEach(
                x -> {
                  int y = stateIndexes[x];
                  states[x] = stateArrays[x][y];
                });
      };
  protected VectorOdometer vectorOdometer;

  protected BaseVectorIterator(VectorOdometer vectorOdometer) {
    this.vectorOdometer = vectorOdometer;
  }

  public void preRunLogic() {}

  protected void iterateInner(ObjIntConsumer<VectorOdometer> indexConsumer) {
    iterateInner(indexConsumer, BLANK_CONSUMER);
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

  protected void iterateOuter(Runnable consumer) {
    iterateOuter((o, i) -> consumer.run(), BLANK_CONSUMER);
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
  protected interface UpdateConsumer {
    void update(int currentIndex, boolean overflow, VectorOdometer odometer);
  }
}
