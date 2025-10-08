package io.github.alecredmond.application.printer;

import lombok.Data;

@Data
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
}
