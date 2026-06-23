package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.method.network.BayesianNetwork;

/** Thrown if a {@link BayesianNetwork}'s structure is in an illegal configuration. */
public class NetworkStructureException extends IllegalArgumentException {
  public NetworkStructureException(String s) {
    super(s);
  }
}
