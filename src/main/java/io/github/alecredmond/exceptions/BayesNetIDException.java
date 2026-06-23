package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.method.network.BayesianNetwork;

/** Thrown to indicate an illegal identifier was used in a {@link BayesianNetwork}. */
public class BayesNetIDException extends IllegalArgumentException {
  public BayesNetIDException(String s) {
    super(s);
  }
}
