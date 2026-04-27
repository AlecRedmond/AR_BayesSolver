package io.github.alecredmond.internal.method.probabilitytables.transfer;

import io.github.alecredmond.internal.method.probabilitytables.transfer.readwriters.TransferIterator;
import io.github.alecredmond.internal.method.probabilitytables.transfer.readwriters.TransferReader;

public class TableTransfer {
  private final TransferReader reader;
  private final TransferIterator writer;

  public TableTransfer(TransferReader reader, TransferIterator writer) {
    this.reader = reader;
    this.writer = writer;
  }

  public void transfer() {
    reader.performRun();
    writer.performRun();
  }
}
