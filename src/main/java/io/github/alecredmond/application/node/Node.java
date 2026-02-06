package io.github.alecredmond.application.node;

import io.github.alecredmond.method.node.NodeUtils;
import java.util.Collection;
import java.util.List;

public interface Node {

  static <T, E> Node build(T nodeID, Collection<E> stateIDs) {
    return new NodeDefault(nodeID, stateIDs);
  }

  static <T> Node build(T nodeID) {
    return new NodeDefault(nodeID);
  }

  default <T> NodeState addState(T stateID) {
    return NodeUtils.addNodeState(this, stateID);
  }

  default void addParent(Node parent) {
    NodeUtils.addParent(this, parent);
  }

  <T> T getId();

  default void removeParent(Node parent) {
    NodeUtils.removeParent(this, parent);
  }

  List<Node> getParents();

  void setParents(List<Node> parents);

  List<Node> getChildren();

  void setChildren(List<Node> children);

  default <E> void removeState(E stateID) {
    NodeUtils.removeState(this, stateID);
  }

  List<NodeState> getNodeStates();

  void setNodeStates(List<NodeState> states);
}
