package io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerInitializerUtils;
import java.util.function.Function;

public interface OdometerResetOnlyOnBuild extends OdometerResetBase {
  @Override
  default void updateInnerInitializer(
      OdometerInitializer innerInitializer, VectorOdometer odometer, boolean[] positionLocks) {
    OdometerInitializerUtils.updateStartIndex(innerInitializer, odometer);
  }

  default Function<Node, boolean[]> buildEvidenceMaps() {
    return node -> null;
  }
}
