package io.github.alecredmond.internal.method.utils;

public enum AppProperty {
  // Directory Properties
  DIRECTORY_ROOT("app.directory.rootDirectory"),
  DIRECTORY_PRINTER("app.directory.printerDirectory"),
  DIRECTORY_SAVE("app.directory.saveDirectory"),

  // Inference Properties
  INFERENCE_USE_JTA_SOLVER("app.inference.useJunctionTreeSolver"),
  INFERENCE_USE_JTA_INFERENCE("app.inference.useJunctionTreeInference"),

  // Solver Properties
  SOLVER_CYCLES_LIMIT("app.solver.cyclesLimit"),
  SOLVER_TIME_LIMIT_SECONDS("app.solver.timeLimitSeconds"),
  SOLVER_LOG_PROGRESS("app.solver.logSolverProgress"),
  SOLVER_LOG_INTERVAL_SECONDS("app.solver.logIntervalSeconds"),
  SOLVER_CONVERGE_THRESHOLD("app.solver.convergeThreshold"),

  // Printer Properties
  PRINTER_OPEN_FILE_ON_CREATION("app.printer.openFileOnCreation"),
  PRINTER_OPEN_FOLDER_ON_CREATION("app.printer.openFolderOnCreation"),
  PRINTER_PRINT_TO_CONSOLE("app.printer.printToConsole"),
  PRINTER_PRINT_TO_TEXT_FILE("app.printer.printToTextFile"),
  PRINTER_OBSERVED_FILE_TITLE("app.printer.observedFileTitle"),
  PRINTER_NETWORK_FILE_TITLE("app.printer.networkFileTitle"),
  PRINTER_PROB_DECIMAL_PLACES("app.printer.probDecimalPlaces"),

  // Extension Properties
  EXTENSION_FILE_TYPE("app.extension.fileExtension");

  private final String key;

  AppProperty(String key) {
    this.key = key;
  }

  public String get() {
    return key;
  }
}
