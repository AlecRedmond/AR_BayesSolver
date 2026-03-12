package io.github.alecredmond.method.probabilitytables.transfer;

import io.github.alecredmond.application.probabilitytables.export.probabilityvector.ProbabilityVector;
import io.github.alecredmond.application.probabilitytables.internal.probabilityvector.TransferIteratorData;
import io.github.alecredmond.application.probabilitytables.internal.probabilityvector.VectorCombinationKey;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

public class MarginalTransferIterator extends TransferIterator {

  public MarginalTransferIterator(TransferIteratorData readData, TransferIteratorData writeData) {
    super(readData, writeData);
  }

  public void transfer() {
    double[] transferArray = new double[readData.getIterationSteps()];
    fillTransferArray(readData, transferArray);
    sumTransferToOutput(writeData, transferArray);
  }

  private void sumTransferToOutput(TransferIteratorData outputData, double[] transferArray) {
    AtomicInteger i = new AtomicInteger();
    VectorCombinationKey transferKey = outputData.getTransferKey();
    ProbabilityVector vector = outputData.getTableVector();
    DoubleAdder adder = new DoubleAdder();
    double[] p = vector.getProbabilities();
    ITERATOR.iterateConditions(
        vector,
        transferKey,
        (conditionKey, conditionIndex) -> {
          ITERATOR.iterateEvents(
              vector, transferKey, (eventKey, eventIndex) -> adder.add(p[eventIndex]));
          double actual = adder.sumThenReset();
          double expected = transferArray[i.getAndAdd(1)];
          double ratio = actual == 0.0 ? 0.0 : expected / actual;
          ITERATOR.iterateEvents(
              vector, transferKey, (eventKey, eventIndex) -> p[eventIndex] = p[eventIndex] * ratio);
        });
  }
}
