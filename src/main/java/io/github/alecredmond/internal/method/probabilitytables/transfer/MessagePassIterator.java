package io.github.alecredmond.internal.method.probabilitytables.transfer;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.TransferIteratorData;
import io.github.alecredmond.internal.application.probabilitytables.probabilityvector.VectorCombinationKey;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class MessagePassIterator extends TransferIterator {
  private final ProbabilityVector separatorVector;

  public MessagePassIterator(
      TransferIteratorData readData,
      TransferIteratorData writeData,
      ProbabilityVector separatorVector) {
    super(readData, writeData);
    this.separatorVector = separatorVector;
  }

  public void transfer() {
    double[] transferArray = new double[readData.getIterationSteps()];
    fillTransferArray(readData, transferArray);
    double[] ratioArray = new double[readData.getIterationSteps()];
    fillRatioArray(transferArray, ratioArray);
    messagePassTransferToOutput(writeData, ratioArray);
    setNewSeparators(transferArray);
  }

  private void fillRatioArray(double[] transferArray, double[] ratioArray) {
    double[] sepP = separatorVector.getProbabilities();
    IntStream.range(0, sepP.length)
        .forEach(i -> ratioArray[i] = sepP[i] == 0.0 ? 0.0 : transferArray[i] / sepP[i]);
  }

  private void messagePassTransferToOutput(TransferIteratorData data, double[] ratioArray) {
    AtomicInteger i = new AtomicInteger();
    VectorCombinationKey transferKey = data.getTransferKey();
    ProbabilityVector vector = data.getTableVector();
    double[] p = vector.getProbabilities();
    ITERATOR.iterateConditions(
        vector,
        transferKey,
        (conditionKey, conditionIndex) -> {
          double ratio = ratioArray[i.getAndAdd(1)];
          ITERATOR.iterateEvents(vector, transferKey, (k, index) -> p[index] = p[index] * ratio);
        });
  }

  private void setNewSeparators(double[] transferArray) {
    double[] sepP = separatorVector.getProbabilities();
    System.arraycopy(transferArray, 0, sepP, 0, sepP.length);
  }
}
