package io.github.alecredmond.method.probabilitytables.transfer;

import io.github.alecredmond.application.probabilitytables.export.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.internal.probabilityvector.TransferIteratorData;
import io.github.alecredmond.application.probabilitytables.internal.probabilityvector.VectorCombinationKey;
import io.github.alecredmond.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

public abstract class TransferIterator {
  protected static final ProbabilityVectorIterator ITERATOR = new ProbabilityVectorIterator();
  protected final TransferIteratorData readData;
  protected final TransferIteratorData writeData;

  protected TransferIterator(TransferIteratorData readData, TransferIteratorData writeData) {
    this.readData = readData;
    this.writeData = writeData;
  }

  public abstract void transfer();

  protected void fillTransferArray(TransferIteratorData data, double[] transferArray) {
    AtomicInteger i = new AtomicInteger();
    VectorCombinationKey transferKey = data.getTransferKey();
    ProbabilityVector vector = data.getTableVector();
    DoubleAdder adder = new DoubleAdder();
    double[] p = vector.getProbabilities();
    ITERATOR.iterateConditions(
        vector,
        transferKey,
        (conditionKey, conditionIndex) -> {
          ITERATOR.iterateEvents(
              vector, transferKey, (eventKey, eventIndex) -> adder.add(p[eventIndex]));
          transferArray[i.getAndAdd(1)] = adder.sumThenReset();
        });
  }
}
