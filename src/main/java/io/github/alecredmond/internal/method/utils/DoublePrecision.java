package io.github.alecredmond.internal.method.utils;

public class DoublePrecision {
  private static final double PRECISION_DELTA =
      new PropertiesLoader().loadDouble(AppProperty.INTERNAL_DOUBLE_EQUALITY);

  private DoublePrecision() {}

  public static boolean fuzzyEquals(double x, double y) {
    return Math.abs(x - y) <= PRECISION_DELTA;
  }
}
