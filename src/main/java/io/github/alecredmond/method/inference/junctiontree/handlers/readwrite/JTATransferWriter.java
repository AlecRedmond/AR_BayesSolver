package io.github.alecredmond.method.inference.junctiontree.handlers.readwrite;

public class JTATransferWriter {
  private final JTAReader read;
  private final JTAWriterMessagePass write;
  private final JTAReadWriteSynchronizer synchronizer;

  public JTATransferWriter(
      JTAReader read, JTAWriterMessagePass write, JTAReadWriteSynchronizer synchronizer) {
    this.read = read;
    this.write = write;
    this.synchronizer = synchronizer;
  }

  public void setToUnityAndRun() {
    write.setDestinationToUnity();
    run();
  }

  public void run() {
    synchronizer.reset();
    new Thread(read).start();
    new Thread(write).start();
  }
}
