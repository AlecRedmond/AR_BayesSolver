package io.github.alecredmond.internal.application.printer;

import static io.github.alecredmond.internal.method.utils.AppProperty.*;

import io.github.alecredmond.internal.method.utils.PropertiesLoader;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class PrinterPropertyConfigs {
  private String saveDirectory;
  private boolean printToConsole;
  private boolean printToTextFile;
  private boolean openFileOnCreation;
  private boolean openFolderOnCreation;
  private int probDecimalPlaces;
  private int probabilityCharLength;
  private String probabilityFormatter;

  public PrinterPropertyConfigs() {
    PropertiesLoader loader = new PropertiesLoader();
    setSaveDirectory(loader.loadDirectory(DIRECTORY_ROOT, DIRECTORY_PRINTER));
    setOpenFileOnCreation(loader.loadBoolean(PRINTER_OPEN_FILE_ON_CREATION));
    setOpenFolderOnCreation(loader.loadBoolean(PRINTER_OPEN_FOLDER_ON_CREATION));
    setPrintToConsole(loader.loadBoolean(PRINTER_PRINT_TO_CONSOLE));
    setProbDecimalPlaces(loader.loadInt(PRINTER_PROB_DECIMAL_PLACES));
    setPrintToTextFile(loader.loadBoolean(PRINTER_PRINT_TO_TEXT_FILE));
    probabilityCharLength = probDecimalPlaces + 2; // Two extra places for "0." or "1."
    probabilityFormatter = "%." + probDecimalPlaces + "f";
  }

  private void setProbDecimalPlaces(int probDecimalPlaces) {
    if (probDecimalPlaces < 0) {
      probDecimalPlaces = 0;
    }
    if (probDecimalPlaces <= 1) {
      log.warn(
          "A low number of decimal places were selected! This may not give the results good resolution!");
    }
    this.probDecimalPlaces = probDecimalPlaces;
  }
}
