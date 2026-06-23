package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;

/** Thrown to indicate an illegal {@link ProbabilityConstraint} configuration was found. */
public class ConstraintValidationException extends IllegalArgumentException {
  public ConstraintValidationException(String s) {
    super(s);
  }
}
