package io.github.alecredmond.application.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"parents"})
public class Node {
  private final Object nodeID;
  private List<NodeState> nodeStates;
  private List<Node> parents;
  private List<Node> children;

  public <T, E> Node(T nodeID, Collection<E> stateIDs) {
    this.nodeID = nodeID;
    this.parents = new ArrayList<>();
    this.children = new ArrayList<>();
    this.nodeStates = new ArrayList<>();
    stateIDs.forEach(this::addState);
  }

  public <T> NodeState addState(T stateID) {
    NodeState newState = new NodeState(stateID, this);
    nodeStates.add(newState);
    return newState;
  }

  public <T> Node(T nodeID) {
    this.nodeID = nodeID;
    this.parents = new ArrayList<>();
    this.children = new ArrayList<>();
    this.nodeStates = new ArrayList<>();
  }

  public void addParent(Node parent) {
    parents.add(parent);
    parent.getChildren().add(this);
  }

  public void removeParent(Node parent) {
    parents.remove(parent);
    parent.getChildren().remove(this);
  }

  public <E> void removeState(E stateID) {
    nodeStates.removeIf(state -> state.getStateID().equals(stateID));
  }

  @Override
  public String toString() {
    return nodeID.toString();
  }
}
