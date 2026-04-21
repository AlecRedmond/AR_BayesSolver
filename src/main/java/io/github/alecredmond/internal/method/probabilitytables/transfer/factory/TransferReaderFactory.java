package io.github.alecredmond.internal.method.probabilitytables.transfer.factory;

import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.transfer.readwriters.TransferReader;
import java.util.function.Supplier;

public class TransferReaderFactory extends TransferReadWriteFactory<TransferReader> {
  protected TransferReaderFactory(ProbabilityTable readTable, ProbabilityTable writeTable) {
    super(readTable, writeTable);
  }

  @Override
  protected ProbabilityTable selectTable() {
    return readTable;
  }

  @Override
  protected Supplier<TransferReader> supplyIterator() {
    return () -> new TransferReader(vectorOdometer, transferArray);
  }
}
