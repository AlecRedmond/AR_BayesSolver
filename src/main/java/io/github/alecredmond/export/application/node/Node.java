package io.github.alecredmond.export.application.node;

import static io.github.alecredmond.internal.method.network.changehandlers.NetworkPropertyChangeEvent.*;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.export.method.network.BayesianNetwork;
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
import lombok.extern.slf4j.Slf4j;

/**
 * A Node representing one of the variables in the Bayesian Network. Each node contains a list of
 * its variables - called {@link NodeState} in AR_BayesSolver - as well as its parents and children.
 * A node is conditionally dependent on its parents, while the children are conditionally dependent
 * on the node.
 *
 * <p>Note: All nodes in a network MUST be registered in the {@link BayesianNetwork} instance before
 * attempting to connect them to their parents or children through the methods defined here.
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Slf4j
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
    try {
      support.firePropertyChange(NODE_STATES_UPDATED.name(), oldStates, nodeStates);
    } catch (BayesNetIDException e) {
      log.error("Error setting states, '{}' reverting...", e.getMessage());
      this.nodeStates = oldStates;
    }
  }


  public void setParents(List<Node> parents) {
    List<Node> oldParents = this.parents;
    this.parents = Collections.unmodifiableList(parents);
    try {
      support.firePropertyChange(NODE_PARENTS_UPDATED.name(), oldParents, parents);
    } catch (BayesNetIDException e) {
      log.error("Error setting parents, '{}' reverting...", e.getMessage());
      this.parents = oldParents;
    }
  }

  public void setChildren(List<Node> children) {
    List<Node> oldChildren = this.children;
    this.children = Collections.unmodifiableList(children);
    try {
      support.firePropertyChange(NODE_CHILDREN_UPDATED.name(), oldChildren, children);
    } catch (BayesNetIDException e) {
      log.error("Error setting children, '{}' reverting...", e.getMessage());
      this.children = oldChildren;
    }
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
