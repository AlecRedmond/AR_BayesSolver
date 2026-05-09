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
  }

  public OdometerInitializer getInitInner() {
    resetLogic.updateInitializer(initInner, odometer, odometer.getInnerIteratorLocks());
    return initInner;
  }

  /*

  public OdometerInitializer getInitOuter() {
    return OdometerInitializerBuilder.initIterateOuter(odometer);
  }

   */

  public void reset() {
    resetLogic.resetOdometer(odometer);
    resetInitializers();
  }

  public void resetInitializers() {
    initInner = OdometerInitializerUtils.initIterateInner(odometer);
    initOuter = OdometerInitializerUtils.initIterateOuter(odometer);
  }
}
