package io.github.alecredmond.internal.method.vectoriterator.iteratorutils.updatelogictypes;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerUpdateLogic;
import java.util.stream.IntStream;

public interface StateUpdater extends OdometerUpdateLogic<VectorOdometer> {
  default void update(VectorOdometer odometer, int index) {
    NodeState[][] stateArrays = odometer.getStateArrays();
    NodeState[] states = odometer.getStates();
    int[] stateIndexes = odometer.getStateIndexes();
    IntStream.range(0, states.length)
        .forEach(
            x -> {
              int y = stateIndexes[x];
              states[x] = stateArrays[x][y];
            });
  }
}
