package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.probabilitytables.ProbabilityTable;

/** Thrown if a {@link ProbabilityTable} would be constructed in an illegal configuration. */
public class TableBuilderException extends IllegalArgumentException {
  /**
   * Constructs a {@code TableBuilderException} with the specified detail message.
   *
   * @param message the detail message.
   */
  public TableBuilderException(String message) {
    super(message);
  }
}
