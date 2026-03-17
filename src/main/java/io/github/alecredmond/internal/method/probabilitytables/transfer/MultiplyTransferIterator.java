package io.github.alecredmond.internal.method.probabilitytables.transfer;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.TransferIteratorData;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorCombinationKey;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiplyTransferIterator extends TransferIterator {

  protected MultiplyTransferIterator(
      TransferIteratorData readData, TransferIteratorData writeData) {
    super(readData, writeData);
  }

  @Override
  public void transfer() {
    double[] transferArray = new double[readData.getIterationSteps()];
    fillTransferArray(readData, transferArray);
    multiplyTransferToOutput(writeData, transferArray);
  }

  private void multiplyTransferToOutput(TransferIteratorData output, double[] transferArray) {
    AtomicInteger i = new AtomicInteger();
    VectorCombinationKey transferKey = output.getTransferKey();
    ProbabilityVector vector = output.getTableVector();
    double[] p = vector.getProbabilities();
    ITERATOR.iterateConditions(
        vector,
        transferKey,
        (conditionKey, conditionIndex) -> {
          double ratio = transferArray[i.getAndAdd(1)];
          ITERATOR.iterateEvents(
              vector, transferKey, (eventKey, eventIndex) -> p[eventIndex] = p[eventIndex] * ratio);
        });
  }
}
