package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;

/** Thrown if a {@link ProbabilityVector} would be built in an illegal configuration. */
public class ProbabilityVectorFactoryException extends IllegalArgumentException {
  public ProbabilityVectorFactoryException(String s) {
    super(s);
  }
}
