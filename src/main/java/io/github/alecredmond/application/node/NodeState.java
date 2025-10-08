package io.github.alecredmond.application.node;

import lombok.Getter;

/**
 * Represents a specific state or value that a {@link Node} in a Bayesian Network can assume. Each
 * NodeState is uniquely identified and linked to its parent Node.
 */
@Getter
public class NodeState {
  /** The unique identifier for this state. */
  private final Object stateID;

  /** The {@link Node} that this state belongs to. */
  private final Node parentNode;

  /**
   * Constructs a new NodeState with an identifier and a reference to its parent node.
   *
   * @param stateID The unique ID to be associated with this state.
   * @param parentNode The {@link Node} to which this state belongs.
   * @param <T> The class of the state's ID.
   */
  public <T> NodeState(T stateID, Node parentNode) {
    this.stateID = stateID;
    this.parentNode = parentNode;
  }

  /**
   * Returns the string representation of the state, which is the string representation of its ID.
   *
   * @return A string representation of the state ID.
   */
  @Override
  public String toString() {
    return stateID.toString();
  }
}
