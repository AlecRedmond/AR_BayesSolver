package io.github.alecredmond.internal.method.vectoriterator.iteratorutils;

import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;

public interface OdometerUpdateLogic<T extends VectorOdometer> {
  void update(T odometer, int index);
}
