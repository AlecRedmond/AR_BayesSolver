package com.artools.application.node;

import lombok.Getter;

@Getter
public class NodeState {
  private final Object stateID;
  private final Node parentNode;

  public <T> NodeState(T stateID, Node parentNode) {
    this.stateID = stateID;
    this.parentNode = parentNode;
  }

  @Override
  public String toString() {
    return stateID.toString();
  }
}
