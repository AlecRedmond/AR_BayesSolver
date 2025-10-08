package io.github.alecredmond.application.printer;

import lombok.Data;

/**
 * Configuration settings for controlling the output and saving of results from the Bayesian Network
 * solver.
 *
 * <p>This class holds settings such as the output directory, console printing preference,
 * file-opening behavior, and the precision for displaying probability values.
 */
@Data
public class PrinterConfigs {
  private String saveDirectory;
  private boolean openFileOnCreation;
  private boolean printToConsole;
  private int probDecimalPlaces;

  /**
   * Default constructor that initializes configuration settings with standard values. <br>
   * {@code printToConsole} is set to {@code false}<br>
   * {@code probDecimalPlaces} is set to 5<br>
   * {@code openFileOnCreation} is set to {@code true}<br>
   * {@code saveDirectory} is set to a default path based on the user's home directory
   */
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
