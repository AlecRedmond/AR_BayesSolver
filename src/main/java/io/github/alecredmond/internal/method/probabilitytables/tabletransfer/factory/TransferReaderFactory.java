package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory;

import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.TransferReader;

public class TransferReaderFactory extends TransferReadWriteFactory<TransferReader> {
  protected TransferReaderFactory(ProbabilityTable readTable, ProbabilityTable writeTable) {
    super(readTable, writeTable);
  }

  @Override
  public TransferReader build() {
    return new TransferReader(readTable.getVector(), transferArray, this);
  }
}
