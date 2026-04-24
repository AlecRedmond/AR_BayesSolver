package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.OdometerInitializer;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorOdometer;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerResetLogic;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.iteratorutils.OdometerSetter;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;
import lombok.Data;

@Data
public class BaseVectorIterator {
  protected static final ObjIntConsumer<VectorOdometer> UPDATE_STATES =
      (odometer, index) -> {
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
  protected ObjIntConsumer<VectorOdometer> updateConsumer;
  protected OdometerSetter odometerSetter;

  public BaseVectorIterator(
      ProbabilityVector vector,
      OdometerResetLogic logic,
      ObjIntConsumer<VectorOdometer> updateConsumer) {
    this.vectorOdometer = OdometerUtils.blankOdometer(vector);
    this.odometerSetter = new OdometerSetter(vectorOdometer, logic);
    this.updateConsumer = updateConsumer;
    this.odometerSetter.set();
  }

  public BaseVectorIterator(ProbabilityVector vector, OdometerResetLogic logic) {
    this.vectorOdometer = OdometerUtils.blankOdometer(vector);
    this.odometerSetter = new OdometerSetter(vectorOdometer, logic);
    this.updateConsumer = (o, i) -> {};
    this.odometerSetter.set();
  }

  public void iterateInner(ObjIntConsumer<VectorOdometer> indexConsumer) {
    iterateInner(indexConsumer, updateConsumer);
  }

  protected void iterateInner(
      ObjIntConsumer<VectorOdometer> indexConsumer, ObjIntConsumer<VectorOdometer> updateConsumer) {
    iterate(
        vectorOdometer,
        indexConsumer,
        updateConsumer,
        OdometerUtils.initIterateInner(vectorOdometer));
  }

  /**
   * This method exists to solve the problem of stepping iteratively through known combinations of
   * states while accessing associated array index of the vector's probability table.
   *
   * <p>Both the Vector Odometer (which can be read for the current state values) and the index (the
   * position in the vector's probability array of the combination) can be iterated through and
   * processed sequentially with a BiConsumer, while locking specific NodeState values in place.
   *
   * <p>It achieves this by advancing the odometer's state index array, starting from the fastest
   * (rightmost) unlocked position and carrying left for every overflow encountered. An overflow
   * that carries to the leftmost position represents an overflow/reset of the odometer as a whole
   * and therefore the end of the iterator cycle.
   *
   * <p>Simultaneously, the index is incremented by the size of the base stride (1 if the fastest
   * moving position (fp) is the rightmost, stepMultiplier[fp] otherwise). When an overflow leads
   * into a locked position (lp), the index is increased by
   *
   * <p><code>
   * (numberOfStates[lp] - 1) * nodeMultiplier[lp])</code>
   *
   * <p>which represents a stride over the other states in the position.
   *
   * <p>There is the option for a second consumer which is run at the end of each update. This may
   * be used e.g. for updating the NodeState[] array to be in-line with the given int[]
   * StateIndexes, but is unused in most cases to reduce compute time per iteration.
   */
  protected void iterate(
      VectorOdometer odometer,
      ObjIntConsumer<VectorOdometer> indexConsumer,
      ObjIntConsumer<VectorOdometer> updateConsumer,
      OdometerInitializer initializer) {
    int currentIndex = initializer.getInitialIndex();

    if (initializer.isFireOnlyOnce()) {
      indexConsumer.accept(odometer, currentIndex);
      return;
    }

    int fastestPosition = initializer.getFastestPosition();
    int baseStride = initializer.getBaseStride();
    int[] numberOfStates = odometer.getNumberOfStates();
    int[] strideIfLocked = initializer.getStrideIfLocked();
    int[] stateIndexes = odometer.getStateIndexes();
    boolean[] positionLocked = initializer.getLockedPositions();
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
        updateConsumer.accept(odometer, currentIndex);
        if (!overflow) {
          break;
        }
      }
    }
  }

  public void reset() {
    odometerSetter.set();
  }

  public void iterateOuter(Runnable runnable) {
    iterateOuter((o, i) -> runnable.run(), updateConsumer);
  }

  protected void iterateOuter(
      ObjIntConsumer<VectorOdometer> indexConsumer, ObjIntConsumer<VectorOdometer> updateConsumer) {
    iterate(
        vectorOdometer,
        indexConsumer,
        updateConsumer,
        OdometerUtils.initIterateOuter(vectorOdometer));
  }

  @FunctionalInterface
  public interface UpdateConsumer {
    void update(int currentIndex, boolean overflow, VectorOdometer odometer);
  }
}
