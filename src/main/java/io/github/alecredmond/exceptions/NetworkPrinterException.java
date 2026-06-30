package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.method.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import java.io.IOException;

/**
 * Thrown if an {@link IOException} or {@link SecurityException} occurred during a printing
 * operation called by {@link BayesianNetwork} or {@link InferenceEngine}.
 */
public class NetworkPrinterException extends RuntimeException {

  /**
   * Constructs a {@code NetworkPrinterException} with the specified cause.
   *
   * @param cause the cause of the exception.
   */
  public NetworkPrinterException(Throwable cause) {
    super(cause);
  }
}
