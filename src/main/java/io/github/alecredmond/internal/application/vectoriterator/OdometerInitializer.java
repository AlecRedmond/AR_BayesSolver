package io.github.alecredmond.internal.application.vectoriterator;

import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerInitializerUtils;
import lombok.Data;

@Data
public class OdometerInitializer {
  private boolean[] lockedPositions;
  private int fastestPosition;
  private boolean fireOnlyOnce;
  private int baseStride;
  private int[] strideIfLocked;
  private int initialIndex;

  public OdometerInitializer(VectorOdometer odometer) {
    this.strideIfLocked = OdometerInitializerUtils.buildStrideIfLocked(odometer);
  }
}
