package io.github.alecredmond.method.inference.junctiontree.handlers.readwrite;

import io.github.alecredmond.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;
import java.util.concurrent.atomic.DoubleAdder;

public class JTAWriter extends Thread {
  private final JTAReadWriteSynchronizer synchronizer;
  private final ProbabilityVector vector;
  private final VectorCombinationKey writeKey;
  private final ProbabilityVectorIterator iterator;

  public JTAWriter(
      JTAReadWriteSynchronizer synchronizer,
      ProbabilityVector vector,
      VectorCombinationKey writeKey) {
    this.synchronizer = synchronizer;
    this.vector = vector;
    this.writeKey = writeKey;
    this.iterator = new ProbabilityVectorIterator();
  }

  @Override
  public void run() {
    int[] tumblerKey = writeKey.getTumblerKey();
    boolean[] outerLock = writeKey.getOuterLock();
    boolean[] innerLock = writeKey.getInnerLock();
    double[] probabilities = vector.getProbabilities();
    DoubleAdder adder = new DoubleAdder();
    synchronized (this.synchronizer) {
      iterator.iterateKeyCombos(
          vector,
          tumblerKey,
          outerLock,
          (key, index) -> {
            iterator.iterateKeyCombos(
                vector, key, innerLock, (k, i) -> adder.add(probabilities[i]));
            double sum = adder.sumThenReset();
            double expected = synchronizer.getSum();
            double ratio = sum == 0.0 ? 0.0 : expected / sum;
            iterator.iterateKeyCombos(
                vector, key, innerLock, (k, i) -> probabilities[i] = ratio * probabilities[i]);
          });
    }
  }
}
