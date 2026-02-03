package io.github.alecredmond.method.inference.junctiontree.handlers.readwrite;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JTATransferWriter {
  private final JTAReader read;
  private final JTAWriter write;
  private final JTAReadWriteSynchronizer synchronizer;

  public JTATransferWriter(JTAReader read, JTAWriter write, JTAReadWriteSynchronizer synchronizer) {
    this.read = read;
    this.write = write;
    this.synchronizer = synchronizer;
  }

  public synchronized void setToUnityAndRun() {
    write.setDestinationToUnity();
    run();
  }

  public synchronized void run() {
    synchronizer.reset();
    Thread readThread = new Thread(read);
    Thread writeThread = new Thread(write);
    readThread.start();
    writeThread.start();
    try {
      while (readThread.isAlive() || writeThread.isAlive()) {
        wait(1);
      }
    } catch (InterruptedException e) {
      readThread.interrupt();
      writeThread.interrupt();
      log.error(e.toString());
    }
  }
}
