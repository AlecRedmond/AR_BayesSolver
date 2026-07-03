package io.github.alecredmond.internal.method.vectoriterator.iteratorutils.updatelogictypes;

import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerUpdateLogic;
import java.util.function.ObjIntConsumer;

public interface OdometerUpdateBlank extends OdometerUpdateLogic<VectorOdometer> {
  default ObjIntConsumer<VectorOdometer> update() {
    return (o, i) -> {};
  }
}
