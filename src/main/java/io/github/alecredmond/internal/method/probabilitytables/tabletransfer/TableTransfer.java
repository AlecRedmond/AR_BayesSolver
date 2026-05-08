package io.github.alecredmond.internal.method.probabilitytables.tabletransfer;

import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.TransferIterator;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.TransferReader;

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
