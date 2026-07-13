package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.probabilitytables.ProbabilityTable;

/** Thrown if an illegal request was made to a {@link ProbabilityTable}. */
public class ProbabilityTableRequestException extends IllegalArgumentException {
  /**
   * Constructs a {@code ProbabilityTableRequestException} with the specified detail message.
   *
   * @param message the detail message.
   */
  public ProbabilityTableRequestException(String message) {
    super(message);
  }
}
