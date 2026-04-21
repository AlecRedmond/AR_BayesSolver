package io.github.alecredmond.internal.method.probabilitytables.transfer.factory;

import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;
import io.github.alecredmond.internal.method.probabilitytables.transfer.TransferIterator;
import io.github.alecredmond.internal.method.probabilitytables.transfer.readwriters.TransferReader;

public class TransferIteratorFactory {
  public TransferIterator buildMessagePassTransfer(
      ProbabilityTable readTable, ProbabilityTable writeTable, ProbabilityTable separatorTable) {
    TransferReader reader = new TransferReaderFactory(readTable, writeTable).build();
    VectorIterator writer =
        new TransferWriterMessagePassFactory(
                readTable, writeTable, separatorTable, reader.getTransferArray())
            .build();
    return new TransferIterator(reader, writer);
  }

  public TransferIterator buildMarginalTransfer(
      ProbabilityTable readTable, ProbabilityTable writeTable) {
    TransferReader reader = new TransferReaderFactory(readTable, writeTable).build();
    VectorIterator writer =
        new TransferWriterMarginalFactory(readTable, writeTable, reader.getTransferArray()).build();
    return new TransferIterator(reader, writer);
  }

  public TransferIterator buildMultiplyInTransfer(
      ProbabilityTable readTable, ProbabilityTable writeTable) {
    TransferReader reader = new TransferReaderFactory(readTable, writeTable).build();
    VectorIterator writer =
        new TransferWriterMultiplyInFactory(readTable, writeTable, reader.getTransferArray())
            .build();
    return new TransferIterator(reader, writer);
  }
}
