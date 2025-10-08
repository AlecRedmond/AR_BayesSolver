package io.github.alecredmond.application.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a single node in a Bayesian Network, holding its associated states, as well as its
 * parent and child nodes in the network structure. It is primarily identified by a unique ID.
 */
@Data
@EqualsAndHashCode(exclude = {"parents"})
public class Node {
  /** The unique identifier for this Node. */
  private final Object nodeID;

  /** The list of all possible states this Node can take. */
  private List<NodeState> states;

  /** The list of parent Nodes in the Bayesian Network. */
  private List<Node> parents;

  /** The list of child Nodes in the Bayesian Network. */
  private List<Node> children;

  /**
   * Builds a Node and populates the NodeState list using the given values.
   *
   * @param <T> The class of the node ID.
   * @param <E> The class of the state IDs.
   * @param nodeID ID value to be associated with the Node.
   * @param stateIDs collection of IDs which will become the IDs for the node's associated states.
   */
  public <T, E> Node(T nodeID, Collection<E> stateIDs) {
    this.nodeID = nodeID;
    this.parents = new ArrayList<>();
    this.children = new ArrayList<>();
    this.states = new ArrayList<>();
    stateIDs.forEach(this::addState);
  }

  /**
   * Adds a NodeState to the 'states' list, constructing it from the given identifier.
   *
   * @param stateID ID value to be associated with the new NodeState.
   * @param <T> The class of the state's ID.
   * @return The newly created {@link NodeState} object.
   */
  public <T> NodeState addState(T stateID) {
    NodeState newState = new NodeState(stateID, this);
    states.add(newState);
    return newState;
  }

  /**
   * Builds a blank Node without any states. States must be added subsequently.
   *
   * @param nodeID ID value to be associated with the Node.
   * @param <T> The class of the Node's ID.
   */
  public <T> Node(T nodeID) {
    this.nodeID = nodeID;
    this.parents = new ArrayList<>();
    this.children = new ArrayList<>();
    this.states = new ArrayList<>();
  }

  /**
   * Adds a parent to this Node's list of parents.
   *
   * @param parent The parent {@link Node} to add.
   */
  public void addParent(Node parent) {
    parents.add(parent);
  }

  /**
   * Removes a parent from this Node's list of parents.
   *
   * @param parent The parent {@link Node} to remove.
   */
  public void removeParent(Node parent) {
    parents.remove(parent);
  }

  /**
   * Adds a child to this Node's list of children.
   *
   * @param child The child {@link Node} to add.
   */
  public void addChild(Node child) {
    children.add(child);
  }

  /**
   * Removes a child from this Node's list of children.
   *
   * @param child The child {@link Node} to remove.
   */
  public void removeChild(Node child) {
    children.remove(child);
  }

  /**
   * Removes a state from the 'states' list based on its identifier.
   *
   * @param stateID The ID value of the state to remove.
   * @param <E> The class of the state's ID.
   */
  public <E> void removeState(E stateID) {
    states.removeIf(state -> state.getStateID().equals(stateID));
  }

  /**
   * Returns the string representation of the Node, which is the string representation of its ID.
   *
   * @return A string representation of the Node ID.
   */
  @Override
  public String toString() {
    return nodeID.toString();
  }
}
