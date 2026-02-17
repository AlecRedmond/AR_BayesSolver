package io.github.alecredmond.exceptions;

public class PropertiesLoaderException extends RuntimeException {
    public PropertiesLoaderException(String message) {
        super(message);
    }

    public PropertiesLoaderException(Throwable cause) {
        super(cause);
    }
}
