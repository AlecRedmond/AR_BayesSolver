package io.github.alecredmond.application.printer;

import io.github.alecredmond.method.utils.PropertiesLoader;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class PrinterConfigs {
  private String saveDirectory;
  private boolean openFileOnCreation;
  private boolean printToConsole;
  private int probDecimalPlaces;

  public PrinterConfigs() {
    PropertiesLoader loader = new PropertiesLoader();
    setSaveDirectory(loader.loadDirectory("app.printer.saveDirectory"));
    setOpenFileOnCreation(loader.loadBoolean("app.printer.openFileOnCreation"));
    setPrintToConsole(loader.loadBoolean("app.printer.printToConsole"));
    setProbDecimalPlaces(loader.loadInt("app.printer.probDecimalPlaces"));
  }

  private void setProbDecimalPlaces(int probDecimalPlaces) {
    if (probDecimalPlaces < 0) {
      throw new IllegalArgumentException("Printer decimal places must not be negative!");
    }
    if (probDecimalPlaces <= 1) {
      log.warn(
          "A low number of decimal places were selected! This may not give the results good resolution!");
    }
    this.probDecimalPlaces = probDecimalPlaces;
  }
}
