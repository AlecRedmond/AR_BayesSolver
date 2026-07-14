package io.github.alecredmond.internal.method.probabilitytables.printerformat;

import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import io.github.alecredmond.internal.application.printer.PrinterStringMatrix;

public interface PrinterMatrixGenerator {
  PrinterStringMatrix generate(PrinterPropertyConfigs configs);
}
