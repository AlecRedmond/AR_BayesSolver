package io.github.alecredmond.application.node;

import lombok.Getter;

@Getter
public class NodeState {
  private final Object stateID;
  private final Node node;

  public <T> NodeState(T stateID, Node node) {
    this.stateID = stateID;
    this.node = node;
  }

  @Override
  public String toString() {
    return stateID.toString();
  }
}
