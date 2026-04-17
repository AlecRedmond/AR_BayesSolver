package io.github.alecredmond.export.application.node;

import static io.github.alecredmond.internal.method.node.NetworkPropertyChangeEvent.*;

import io.github.alecredmond.internal.method.node.NodeUtils;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Node {
  @EqualsAndHashCode.Include private final Serializable id;
  private final PropertyChangeSupport support = new PropertyChangeSupport(this);
  private List<NodeState> nodeStates;
  private List<Node> parents;
  private List<Node> children;

  public <T extends Serializable, E extends Serializable> Node(
      @NonNull T id, Collection<E> stateIDs) {
    this.id = id;
    this.parents = List.of();
    this.children = List.of();
    this.nodeStates = List.of();
    NodeUtils.addNodeStates(this, stateIDs);
  }

  public <T extends Serializable> Node(T id) {
    this.id = id;
    this.parents = List.of();
    this.children = List.of();
    this.nodeStates = List.of();
  }

  public void setNodeStates(List<NodeState> nodeStates) {
    List<NodeState> oldStates = this.nodeStates;
    this.nodeStates = Collections.unmodifiableList(nodeStates);
    support.firePropertyChange(NODE_STATES_UPDATED.name(), oldStates, nodeStates);
  }

  public void setParents(List<Node> parents) {
    List<Node> oldParents = this.parents;
    this.parents = Collections.unmodifiableList(parents);
    support.firePropertyChange(NODE_PARENTS_UPDATED.name(), oldParents, parents);
  }

  public void setChildren(List<Node> children) {
    List<Node> oldChildren = this.children;
    this.children = Collections.unmodifiableList(children);
    support.firePropertyChange(NODE_CHILDREN_UPDATED.name(), oldChildren, children);
  }

  public <T extends Serializable> NodeState addState(T stateID) {
    return NodeUtils.addNodeState(this, stateID);
  }

  public <E extends Serializable> void removeState(E stateID) {
    NodeUtils.removeState(this, stateID);
  }

  public void addParent(Node parent) {
    NodeUtils.addParent(this, parent);
  }

  public void removeParent(Node parent) {
    NodeUtils.removeParent(this, parent);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
    support.firePropertyChange(NODE_ADDED_TO_NETWORK.name(), null, this);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.firePropertyChange(NODE_REMOVED_FROM_NETWORK.name(), this, null);
    support.removePropertyChangeListener(listener);
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
