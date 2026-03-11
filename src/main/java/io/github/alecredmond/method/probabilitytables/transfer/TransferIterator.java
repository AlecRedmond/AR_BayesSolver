package io.github.alecredmond.method.probabilitytables.transfer;

import io.github.alecredmond.application.probabilitytables.internal.probabilityvector.TransferIteratorData;
import io.github.alecredmond.method.probabilitytables.probabilityvector.ProbabilityVectorIterator;

public abstract class TransferIterator {
  protected static final ProbabilityVectorIterator ITERATOR = new ProbabilityVectorIterator();
  protected final TransferIteratorData readData;
  protected final TransferIteratorData writeData;

  protected TransferIterator(TransferIteratorData readData, TransferIteratorData writeData) {
    this.readData = readData;
    this.writeData = writeData;
  }

  public abstract void transfer();
}
