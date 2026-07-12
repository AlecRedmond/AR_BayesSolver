package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory;

import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.TransferWriterMarginal;

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
