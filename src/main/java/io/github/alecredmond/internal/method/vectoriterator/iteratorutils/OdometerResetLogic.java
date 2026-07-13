package io.github.alecredmond.internal.method.vectoriterator.iteratorutils;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import java.util.function.Predicate;

public interface OdometerResetLogic<T extends VectorOdometer> {
  void resetOdometer(T odometer);

  Predicate<Node> checkLockOuter();

  Predicate<Node> checkLockInner();

  void updateInnerInitializer(
      OdometerInitializer innerInitializer, VectorOdometer odometer, boolean[] positionLocks);
}
