package io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes;

import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerInitializerUtils;

public interface PermanentLocksResetLogic extends BaseOdometerResetLogic {
  @Override
  default void updateInnerInitializer(
      OdometerInitializer innerInitializer, VectorOdometer odometer, boolean[] positionLocks) {
    OdometerInitializerUtils.updateStartIndex(innerInitializer, odometer);
  }
}
