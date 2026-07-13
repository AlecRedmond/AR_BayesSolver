package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory;

import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.TransferIterator;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.TransferReader;

public class TransferIteratorFactory {
  public TableTransfer buildMessagePassTransfer(
      ProbabilityTable readTable, ProbabilityTable writeTable, ProbabilityTable separatorTable) {
    TransferReader reader = new TransferReaderFactory(readTable, writeTable).build();
    TransferIterator writer =
        new TransferWriterMessagePassFactory(
                readTable, writeTable, separatorTable, reader.getTransferArray())
            .build();
    return new TableTransfer(reader, writer);
  }

  public TableTransfer buildMarginalTransfer(
      ProbabilityTable readTable, ProbabilityTable writeTable) {
    TransferReader reader = new TransferReaderFactory(readTable, writeTable).build();
    TransferIterator writer =
        new TransferWriterMarginalFactory(readTable, writeTable, reader.getTransferArray()).build();
    return new TableTransfer(reader, writer);
  }

  public TableTransfer buildMultiplyInTransfer(
      ProbabilityTable readTable, ProbabilityTable writeTable) {
    TransferReader reader = new TransferReaderFactory(readTable, writeTable).build();
    TransferIterator writer =
        new TransferWriterMultiplyInFactory(readTable, writeTable, reader.getTransferArray())
            .build();
    return new TableTransfer(reader, writer);
  }
}
