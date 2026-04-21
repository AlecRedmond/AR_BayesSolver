package io.github.alecredmond.internal.method.probabilitytables.transfer.factory;

import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.transfer.readwriters.TransferWriterMarginal;
import java.util.function.Supplier;

public class TransferWriterMarginalFactory
    extends TransferReadWriteFactory<TransferWriterMarginal> {
  protected TransferWriterMarginalFactory(
      ProbabilityTable readTable, ProbabilityTable writeTable, double[] transferArray) {
    super(readTable, writeTable, transferArray);
  }

  @Override
  protected ProbabilityTable selectTable() {
    return writeTable;
  }

  @Override
  protected Supplier<TransferWriterMarginal> supplyIterator() {
    return () -> new TransferWriterMarginal(vectorOdometer, transferArray);
  }
}
