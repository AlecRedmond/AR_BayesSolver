package io.github.alecredmond.internal.method.probabilitytables.transfer;

import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators.VectorIterator;
import io.github.alecredmond.internal.method.probabilitytables.transfer.readwriters.TransferReader;

public class TransferIterator {
  private final TransferReader reader;
  private final VectorIterator writer;

  public TransferIterator(TransferReader reader, VectorIterator writer) {
    this.reader = reader;
    this.writer = writer;
  }

  public void transfer() {
    reader.performRun();
    writer.performRun();
  }
}
