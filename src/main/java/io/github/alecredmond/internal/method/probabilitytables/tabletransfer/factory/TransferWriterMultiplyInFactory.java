package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory;

import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.TransferWriterMultiplyIn;

public class TransferWriterMultiplyInFactory
    extends TransferReadWriteFactory<TransferWriterMultiplyIn> {
  protected TransferWriterMultiplyInFactory(
      ProbabilityTable readTable, ProbabilityTable writeTable, double[] transferArray) {
    super(readTable, writeTable, transferArray);
  }

  @Override
  public TransferWriterMultiplyIn build() {
    return new TransferWriterMultiplyIn(writeTable.getVector(), transferArray, this);
  }
}
