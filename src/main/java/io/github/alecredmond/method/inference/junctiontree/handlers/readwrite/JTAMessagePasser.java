package io.github.alecredmond.method.inference.junctiontree.handlers.readwrite;

public class JTAMessagePasser {
  private final JTAReader read;
  private final JTAWriter write;

  public JTAMessagePasser(JTAReader read, JTAWriter write) {
    this.read = read;
    this.write = write;
  }

  public void setToUnityAndRun() {
    write.setDestinationToUnity();
    run();
  }

  public void run() {
    read.start();
    write.start();
  }
}
