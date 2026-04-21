package io.github.alecredmond.internal.method.probabilitytables.transfer.factory;

import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.transfer.readwriters.TransferWriterMultiplyIn;
import java.util.function.Supplier;

public class TransferWriterMultiplyInFactory
    extends TransferReadWriteFactory<TransferWriterMultiplyIn> {
  protected TransferWriterMultiplyInFactory(
      ProbabilityTable readTable, ProbabilityTable writeTable, double[] transferArray) {
    super(readTable, writeTable, transferArray);
  }

  @Override
  protected ProbabilityTable selectTable() {
    return writeTable;
  }

  @Override
  protected Supplier<TransferWriterMultiplyIn> supplyIterator() {
    return () -> new TransferWriterMultiplyIn(vectorOdometer, transferArray);
  }
}
