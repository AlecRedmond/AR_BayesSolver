package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.method.inference.BayesSolver;
import io.github.alecredmond.export.method.inference.InferenceEngine;

/**
 * Thrown if a {@link ProbabilityTable} in {@link BayesSolver} or {@link InferenceEngine} would
 * exceed 2<sup>31</sup>&minus;1 entries.
 */
public class TreewidthException extends IllegalStateException {
  public TreewidthException(String message) {
    super(message);
  }
}
