package io.github.alecredmond.internal.application.printer;

import io.github.alecredmond.internal.method.utils.PropertiesLoader;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class PrinterConfigs {
  public static final String LEFT_PAD_FORMAT = "%%%ds";
  public static final String RIGHT_PAD_FORMAT = "%%-%ds";
  private String saveDirectory;
  private boolean printToConsole;
  private boolean printToTextFile;
  private String observedFileTitle;
  private String networkFileTitle;
  private boolean openFileOnCreation;
  private boolean openFolderOnCreation;
  private int probDecimalPlaces;
  private int probabilityCharLength;
  private String probabilityFormatter;

  public PrinterConfigs() {
    PropertiesLoader loader = new PropertiesLoader();
    setSaveDirectory(loader.loadDirectory("app.printer.saveDirectory"));
    setOpenFileOnCreation(loader.loadBoolean("app.printer.openFileOnCreation"));
    setOpenFolderOnCreation(loader.loadBoolean("app.printer.openFolderOnCreation"));
    setPrintToConsole(loader.loadBoolean("app.printer.printToConsole"));
    setProbDecimalPlaces(loader.loadInt("app.printer.probDecimalPlaces"));
    setPrintToTextFile(loader.loadBoolean("app.printer.printToTextFile"));
    setObservedFileTitle(loader.loadString("app.printer.observedFileTitle"));
    setNetworkFileTitle(loader.loadString("app.printer.networkFileTitle"));
    probabilityCharLength = probDecimalPlaces + 2; // Two extra places for "0." or "1."
    probabilityFormatter = "%." + probDecimalPlaces + "f";
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
