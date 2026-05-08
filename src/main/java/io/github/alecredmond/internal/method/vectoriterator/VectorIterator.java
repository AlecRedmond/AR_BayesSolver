package io.github.alecredmond.internal.method.vectoriterator;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerController;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerResetLogic;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerUpdateLogic;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;
import lombok.Data;

@Data
@SuppressWarnings("rawtypes")
public class VectorIterator {
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

  protected OdometerController controller;

  public VectorIterator(OdometerController controller) {
    this.controller = controller;
    controller.reset();
  }

  public <T extends VectorOdometer, R extends OdometerUpdateLogic<T> & OdometerResetLogic<T>>
      VectorIterator(T odometer, R logic) {
    this.controller = new OdometerController<>(odometer, logic, logic);
    controller.reset();
  }

  public <T extends VectorOdometer, R extends OdometerUpdateLogic<T> & OdometerResetLogic<T>>
      VectorIterator(
          ProbabilityVector vector, R logic, Function<ProbabilityVector, T> constructor) {
    T odometer = constructor.apply(vector);
    this.controller = new OdometerController<>(odometer, logic, logic);
    controller.reset();
  }

  public void iterateInner(ObjIntConsumer<VectorOdometer> indexConsumer) {
    iterateInner(indexConsumer, controller::update);
  }

  protected void iterateInner(
      ObjIntConsumer<VectorOdometer> indexConsumer, IntConsumer updateConsumer) {
    iterate(controller.getOdometer(), indexConsumer, updateConsumer, controller.initIterateInner());
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
      IntConsumer updateConsumer,
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
        updateConsumer.accept(currentIndex);
        if (!overflow) {
          break;
        }
      }
    }
  }

  public void reset() {
    controller.reset();
  }

  public void iterateOuter(Runnable runnable) {
    iterateOuter((o, i) -> runnable.run(), controller::update);
  }

  protected void iterateOuter(
      ObjIntConsumer<VectorOdometer> indexConsumer, IntConsumer updateConsumer) {
    iterate(controller.getOdometer(), indexConsumer, updateConsumer, controller.initIterateOuter());
  }
}
