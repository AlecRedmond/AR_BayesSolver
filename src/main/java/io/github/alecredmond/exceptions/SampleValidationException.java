package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.method.sampler.Sampler;

/** Thrown if samples generated in {@link Sampler} failed validation. */
public class SampleValidationException extends RuntimeException {
  public SampleValidationException(String message) {
    super(message);
  }
}
