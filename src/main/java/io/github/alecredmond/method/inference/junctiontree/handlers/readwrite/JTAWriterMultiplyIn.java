package io.github.alecredmond.method.inference.junctiontree.handlers.readwrite;

import io.github.alecredmond.application.probabilitytables.export.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.internal.probabilityvector.VectorCombinationKey;

public class JTAWriterMultiplyIn extends JTAWriter {

  public JTAWriterMultiplyIn(
      JTAReadWriteSynchronizer synchronizer,
      ProbabilityVector vector,
      VectorCombinationKey writeKey) {
    super(synchronizer, vector, writeKey);
  }

  @Override
  public void run() {
    int[] tumblerKey = writeKey.getStateIndexes();
    boolean[] iterateCommon = writeKey.getIterateConditions();
    boolean[] iterateExclusive = writeKey.getIterateEvents();
    double[] probabilities = vector.getProbabilities();

    iterator.iterateKeyCombos(
        vector,
        tumblerKey,
        iterateCommon,
        (key, index) -> {
          synchronized (this.synchronizer) {
            double multiplier = synchronizer.getSum();
            iterator.iterateKeyCombos(
                vector,
                key,
                iterateExclusive,
                (k, i) -> probabilities[i] = multiplier * probabilities[i]);
          }
        });
  }
}
