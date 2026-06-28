package io.github.alecredmond.exceptions;

/** Thrown if an {@code app.properties} field could not be correctly loaded. */
public class PropertiesLoaderException extends RuntimeException {
  public PropertiesLoaderException(Throwable cause) {
    super(cause);
  }

  public PropertiesLoaderException(String string) {
    super(string);
  }
}
