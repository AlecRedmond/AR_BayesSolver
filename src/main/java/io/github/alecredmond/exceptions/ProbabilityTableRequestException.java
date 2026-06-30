package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.method.probabilitytables.TableQueryTool;

/** Thrown if an illegal request was made to a {@link TableQueryTool}. */
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
