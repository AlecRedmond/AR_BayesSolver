package io.github.alecredmond.method.inference.junctiontree.handlers.readwrite;

public class JTAReadWriteSynchronizer {
  private double sum;
  private boolean writeEnable;

  public JTAReadWriteSynchronizer() {
    this.sum = 0.0;
    this.writeEnable = true;
  }

  public synchronized double getSum() {
    while (writeEnable) {
      try {
        wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    double copy = this.sum;
    this.writeEnable = true;
    notify();
    return copy;
  }

  public synchronized void setSum(double sum) {
    while (!writeEnable) {
      try {
        wait();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    this.sum = sum;
    this.writeEnable = false;
    notify();
  }
}
