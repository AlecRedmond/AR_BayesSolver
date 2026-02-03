package io.github.alecredmond.method.inference.junctiontree.handlers.readwrite;

import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;
import java.util.Arrays;

public abstract class JTAWriter implements Runnable {
  protected final JTAReadWriteSynchronizer synchronizer;
  protected final ProbabilityVector vector;
  protected final VectorCombinationKey writeKey;
  protected final ProbabilityVectorIterator iterator;

    protected JTAWriter(
      JTAReadWriteSynchronizer synchronizer,
      ProbabilityVector vector,
      VectorCombinationKey writeKey) {
    this.synchronizer = synchronizer;
    this.vector = vector;
    this.writeKey = writeKey;
    this.iterator = new ProbabilityVectorIterator();
  }

  public synchronized void setDestinationToUnity() {
    Arrays.fill(vector.getProbabilities(), 1.0);
  }
}
