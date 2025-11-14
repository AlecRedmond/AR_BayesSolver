package io.github.alecredmond.application.printer;

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
    this.printToConsole = false;
    this.probDecimalPlaces = 5;
    this.openFileOnCreation = true;
    this.saveDirectory = getDefaultSaveDirectory();
  }

  private String getDefaultSaveDirectory() {
    return System.getProperty("user.home") + "\\AR_Tools\\bayes_solver\\output\\";
  }

  public void setProbDecimalPlaces(int probDecimalPlaces) {
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
