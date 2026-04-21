package io.github.alecredmond.internal.method.probabilitytables.transfer.factory;

import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.transfer.readwriters.TransferWriterMessagePass;
import java.util.function.Supplier;

public class TransferWriterMessagePassFactory
    extends TransferReadWriteFactory<TransferWriterMessagePass> {
  private final ProbabilityVector separatorVector;

  protected TransferWriterMessagePassFactory(
      ProbabilityTable readTable,
      ProbabilityTable writeTable,
      ProbabilityTable separatorTable,
      double[] transferArray) {
    super(readTable, writeTable, transferArray);
    this.separatorVector = separatorTable.getVector();
  }

  @Override
  protected ProbabilityTable selectTable() {
    return writeTable;
  }

  @Override
  protected Supplier<TransferWriterMessagePass> supplyIterator() {
    return () -> new TransferWriterMessagePass(vectorOdometer, transferArray, separatorVector);
  }
}
