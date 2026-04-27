package io.github.alecredmond.exceptions;

public class NodeStateConflictException extends IllegalStateException {
  public NodeStateConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}
