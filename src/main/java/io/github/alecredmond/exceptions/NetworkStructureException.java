package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.method.network.BayesianNetwork;

/** Thrown if a {@link BayesianNetwork}'s structure is in an illegal configuration. */
public class NetworkStructureException extends IllegalArgumentException {
  /**
   * Constructs a {@code NetworkStructureException} with the specified detail message.
   *
   * @param message the detail message.
   */
  public NetworkStructureException(String message) {
    super(message);
  }
}
