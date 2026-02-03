package io.github.alecredmond.method.inference.junctiontree.handlers.readwrite;

import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.probabilityvector.VectorCombinationKey;
import java.util.concurrent.atomic.DoubleAdder;

public class JTACopyInWriter extends JTAWriter {

  public JTACopyInWriter(
      JTAReadWriteSynchronizer synchronizer,
      ProbabilityVector vector,
      VectorCombinationKey writeKey) {
    super(synchronizer, vector, writeKey);
  }

  @Override
  public void run() {
    int[] tumblerKey = writeKey.getTumblerKey();
    boolean[] outerLock = writeKey.getOuterLock();
    boolean[] innerLock = writeKey.getInnerLock();
    double[] probabilities = vector.getProbabilities();

    iterator.iterateKeyCombos(
        vector,
        tumblerKey,
        outerLock,
        (key, index) -> {
          synchronized (this.synchronizer) {
            double multiplier = synchronizer.getSum();
            iterator.iterateKeyCombos(
                vector, key, innerLock, (k, i) -> probabilities[i] = multiplier * probabilities[i]);
          }
        });
  }
}
