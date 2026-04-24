package io.github.alecredmond.internal.method.probabilitytables.transfer.factory;

import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.transfer.readwriters.TransferWriterMarginal;

public class TransferWriterMarginalFactory
    extends TransferReadWriteFactory<TransferWriterMarginal> {
  protected TransferWriterMarginalFactory(
      ProbabilityTable readTable, ProbabilityTable writeTable, double[] transferArray) {
    super(readTable, writeTable, transferArray);
  }

  @Override
  public TransferWriterMarginal build() {
    return new TransferWriterMarginal(writeTable.getVector(), transferArray, this);
  }
}
