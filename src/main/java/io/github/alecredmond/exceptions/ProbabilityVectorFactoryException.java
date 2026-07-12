package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.probabilitytables.ProbabilityVector;

/** Thrown if a {@link ProbabilityVector} would be built in an illegal configuration. */
public class ProbabilityVectorFactoryException extends IllegalArgumentException {
  /**
   * Constructs a {@code ProbabilityVectorFactoryException} with the specified detail message.
   *
   * @param message the detail message.
   */
  public ProbabilityVectorFactoryException(String message) {
    super(message);
  }
}
