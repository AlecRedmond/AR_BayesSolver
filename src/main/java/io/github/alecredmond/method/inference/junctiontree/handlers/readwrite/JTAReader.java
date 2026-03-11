package io.github.alecredmond.method.inference.junctiontree.handlers.readwrite;

import io.github.alecredmond.application.probabilitytables.export.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.internal.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;
import java.util.concurrent.atomic.DoubleAdder;

public class JTAReader implements Runnable {
  private final JTAReadWriteSynchronizer synchronizer;
  private final ProbabilityVector vector;
  private final VectorCombinationKey readKey;
  private final ProbabilityVectorIterator iterator;

  public JTAReader(
      JTAReadWriteSynchronizer synchronizer,
      ProbabilityVector vector,
      VectorCombinationKey readKey) {
    this.synchronizer = synchronizer;
    this.vector = vector;
    this.readKey = readKey;
    this.iterator = new ProbabilityVectorIterator();
  }

  @Override
  public void run() {
    DoubleAdder adder = new DoubleAdder();
    double[] probabilities = vector.getProbabilities();

    iterator.iterateConditions(
        vector,
        readKey,
        (key, index) -> {
          synchronized (this.synchronizer) {
            iterator.iterateEvents(vector, readKey, (k, i) -> adder.add(probabilities[i]));
            double sum = adder.sumThenReset();
            this.synchronizer.setSum(sum);
          }
        });
  }
}
