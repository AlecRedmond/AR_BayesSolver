package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.network.BayesianNetwork;

/** Thrown to indicate an illegal identifier was used in a {@link BayesianNetwork}. */
public class BayesNetIDException extends IllegalArgumentException {
  /**
   * Constructs a {@code BayesNetIDException} with the specified detail message.
   *
   * @param message the detail message.
   */
  public BayesNetIDException(String message) {
    super(message);
  }
}
