package io.github.alecredmond.internal.method.vectoriterator.iteratorutils;

import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import java.util.function.ObjIntConsumer;
import lombok.Data;

@Data
public class OdometerController<T extends VectorOdometer> {
  private T odometer;
  private OdometerResetLogic<T> resetLogic;
  private ObjIntConsumer<T> updateConsumer;
  private OdometerInitializer initInner;
  private OdometerInitializer initOuter;

  public OdometerController(
      T odometer, OdometerResetLogic<T> resetLogic, OdometerUpdateLogic<T> updateLogic) {
    this.odometer = odometer;
    this.resetLogic = resetLogic;
    this.updateConsumer = updateLogic.update();
    this.initOuter = new OdometerInitializer(odometer);
    this.initInner = new OdometerInitializer(odometer);
  }

  public OdometerInitializer getInitInner() {
    resetLogic.updateInnerInitializer(initInner, odometer, odometer.getInnerIteratorLocks());
    return initInner;
  }

  public void reset() {
    resetLogic.resetOdometer(odometer);
    OdometerInitializerUtils.resetInnerInitializer(odometer, initInner);
    OdometerInitializerUtils.resetOuterInitializer(odometer, initOuter);
  }
}
