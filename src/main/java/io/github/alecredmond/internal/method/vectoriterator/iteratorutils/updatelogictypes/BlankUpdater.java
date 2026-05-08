package io.github.alecredmond.internal.method.vectoriterator.iteratorutils.updatelogictypes;

import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerUpdateLogic;

public interface BlankUpdater extends OdometerUpdateLogic<VectorOdometer> {
  default void update(VectorOdometer odometer, int index) {}
}
