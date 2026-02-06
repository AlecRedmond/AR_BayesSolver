package io.github.alecredmond.application.node;

public interface NodeState {
  static <T> NodeState build(T stateID, Node node) {
    return new NodeStateDefault(stateID, node);
  }

  <T> T getId();

  Node getNode();
}
