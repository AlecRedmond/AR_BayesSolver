package com.artools.application.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"parents"})
public class Node {
  private final Object nodeID;
  private List<NodeState> states;
  private List<Node> parents;
  private List<Node> children;

  public <T, E> Node(T nodeID, Collection<E> stateIDs) {
    this.nodeID = nodeID;
    this.parents = new ArrayList<>();
    this.children = new ArrayList<>();
    this.states = new ArrayList<>();
    stateIDs.forEach(this::addState);
  }

  public <T> NodeState addState(T stateIdentifier) {
    NodeState newState = new NodeState(stateIdentifier, this);
    states.add(newState);
    return newState;
  }

  public <T> Node(T nodeID) {
    this.nodeID = nodeID;
    this.parents = new ArrayList<>();
    this.children = new ArrayList<>();
    this.states = new ArrayList<>();
  }

  public void addParent(Node parent) {
    parents.add(parent);
  }

  public void removeParent(Node parent) {
    parents.remove(parent);
  }

  public void addChild(Node child) {
    children.add(child);
  }

  public void removeChild(Node child) {
    children.remove(child);
  }

  public <E> void removeState(E stateID) {
    states.removeIf(state -> state.getStateID().equals(stateID));
  }

  @Override
  public String toString() {
    return nodeID.toString();
  }
}
