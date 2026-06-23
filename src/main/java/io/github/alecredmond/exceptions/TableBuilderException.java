package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;

/** Thrown if a {@link ProbabilityTable} would be constructed in an illegal configuration. */
public class TableBuilderException extends IllegalArgumentException {
  public TableBuilderException(String s) {
    super(s);
  }
}
