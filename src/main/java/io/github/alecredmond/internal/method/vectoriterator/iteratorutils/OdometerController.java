package io.github.alecredmond.internal.method.vectoriterator.iteratorutils;

import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OdometerController<T extends VectorOdometer> {
  private T odometer;
  private OdometerResetLogic<T> resetLogic;
  private OdometerUpdateLogic<T> updateLogic;

  public OdometerInitializer initIterateInner() {
    return OdometerInitializerBuilder.initIterateInner(odometer);
  }

  public OdometerInitializer initIterateOuter() {
    return OdometerInitializerBuilder.initIterateOuter(odometer);
  }

  public void reset() {
    resetLogic.resetOdometer(odometer);
  }

  public void update(int index) {
    updateLogic.update(odometer, index);
  }
}
