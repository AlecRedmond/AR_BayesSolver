package io.github.alecredmond.exceptions;

/** Thrown during a CPT direct mapping run if an event entry cannot be inferred. */
public class CptDirectMappingException extends IllegalArgumentException {
  public CptDirectMappingException(String message) {
    super(message);
  }
}
