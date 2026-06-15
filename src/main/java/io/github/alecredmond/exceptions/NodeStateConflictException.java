package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;

/**
 * An exception thrown when two or more {@link NodeState} objects share a common {@link Node} under
 * circumstances where each {@link Node} must be unique.
 */
public class NodeStateConflictException extends IllegalStateException {
  public NodeStateConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}
