package io.github.alecredmond.method.inference.junctiontree.handlers.readwrite;

import io.github.alecredmond.application.probabilitytables.export.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.internal.probabilityvector.VectorCombinationKey;
import java.util.concurrent.atomic.DoubleAdder;

public class JTAWriterMessagePass extends JTAWriter {
  public JTAWriterMessagePass(
      JTAReadWriteSynchronizer synchronizer,
      ProbabilityVector vector,
      VectorCombinationKey writeKey) {
    super(synchronizer, vector, writeKey);
  }

  @Override
  public void run() {
    int[] tumblerKey = writeKey.getStateKey();
    boolean[] outerLock = writeKey.getOuterLock();
    boolean[] innerLock = writeKey.getInnerLock();
    double[] probabilities = vector.getProbabilities();
    DoubleAdder adder = new DoubleAdder();

    iterator.iterateKeyCombos(
        vector,
        tumblerKey,
        outerLock,
        (key, index) -> {
          synchronized (this.synchronizer) {
            iterator.iterateKeyCombos(
                vector, key, innerLock, (k, i) -> adder.add(probabilities[i]));
            double sum = adder.sumThenReset();
            double expected = synchronizer.getSum();
            double ratio = sum == 0.0 ? 0.0 : expected / sum;
            iterator.iterateKeyCombos(
                vector, key, innerLock, (k, i) -> probabilities[i] = ratio * probabilities[i]);
          }
        });
  }
}
