package io.github.alecredmond.exceptions;

/** Thrown if an {@code app.properties} field could not be correctly loaded. */
public class PropertiesLoaderException extends RuntimeException {
  /**
   * Constructs a {@code PropertiesLoaderException} with the specified cause.
   *
   * @param cause the cause of the exception.
   */
  public PropertiesLoaderException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a {@code PropertiesLoaderException} with the specified detail message.
   *
   * @param message the detail message.
   */
  public PropertiesLoaderException(String message) {
    super(message);
  }
}
