package io.github.alecredmond.exceptions;

/** Thrown during a CPT direct mapping run if an event entry cannot be inferred. */
public class CptDirectMappingException extends IllegalArgumentException {
  /**
   * Constructs a {@code CptDirectMappingException} with the specified detail message.
   *
   * @param message the detail message.
   */
  public CptDirectMappingException(String message) {
    super(message);
  }
}
