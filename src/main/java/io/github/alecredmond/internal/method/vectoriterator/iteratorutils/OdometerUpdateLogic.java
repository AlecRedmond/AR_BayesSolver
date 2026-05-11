package io.github.alecredmond.internal.method.vectoriterator.iteratorutils;

import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;

import java.util.function.ObjIntConsumer;

public interface OdometerUpdateLogic<T extends VectorOdometer> {
  ObjIntConsumer<T> update();
}
