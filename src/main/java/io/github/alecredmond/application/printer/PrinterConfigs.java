package io.github.alecredmond.application.printer;

import io.github.alecredmond.BayesianNetwork;
import lombok.Data;

/**
 * Configuration settings for controlling the output and saving of results from {@link
 * BayesianNetwork} <br>
 * This class holds settings such as the output directory, console printing preference, file-opening
 * behavior, and the precision for displaying probability values.
 */
@Data
public class PrinterConfigs {
  /** Absolute path to the save directory */
  private String saveDirectory;

  /** Flag to toggle whether new files are opened on creation */
  private boolean openFileOnCreation;

  /** Flag to toggle whether the files are printed to the console instead of .txt files */
  private boolean printToConsole;

  /** Number of decimal places the probability values are formatted to */
  private int probDecimalPlaces;

  /**
   * Default constructor that initializes configuration settings with standard values. <br>
   * {@code printToConsole} is set to {@code false}<br>
   * {@code probDecimalPlaces} is set to 5<br>
   * {@code openFileOnCreation} is set to {@code true}<br>
   * {@code saveDirectory} is set to {@code $user_home$/AR_Tools/bayes_solver/output/}
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

  public void setProbDecimalPlaces(int probDecimalPlaces) {
    if (probDecimalPlaces < 0) {
      throw new IllegalArgumentException("Printer decimal places must not be negative!");
    }
    this.probDecimalPlaces = probDecimalPlaces;
  }
}
