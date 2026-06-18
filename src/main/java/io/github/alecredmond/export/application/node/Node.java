package io.github.alecredmond.export.application.node;

import static io.github.alecredmond.internal.method.network.changehandlers.NetworkPropertyChangeEvent.*;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.export.application.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.method.network.BayesianNetwork;
import io.github.alecredmond.export.method.network.BayesianNetworkBuilder;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * A Node representing one of the variables in the {@link BayesianNetwork}. Each node contains a
 * list of its {@link NodeState}s as well as its parents and children. A {@link NodeState} is a
 * state that a {@code Node} can exhibit, such as {@code "NODE:TRUE"} or {@code "NODE:FALSE"}. A
 * {@code Node} is conditionally dependent on its parents, while its children are conditionally
 * dependent on it.
 *
 * <p>Note: All nodes in a network MUST be registered in the {@link BayesianNetwork} instance before
 * attempting to connect them to their parents or children through the methods defined here.
 *
 * @see NodeState
 * @see BayesianNetwork
 * @see BayesianNetworkBuilder
 * @see ProbabilityConstraint
 * @author Alec Redmond
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Slf4j
public class Node {
  /**
   * The identifier for this {@code Node}. This value must be unique within its {@link
   * BayesianNetwork}
   */
  @EqualsAndHashCode.Include private final Serializable id;

  /**
   * Property Change Support, used in {@link BayesianNetwork} when a field change occurs in this
   * {@code Node}.
   */
  private final PropertyChangeSupport support = new PropertyChangeSupport(this);

  /** The ordered list of {@link NodeState} values that this {@code Node} can exhibit. */
  private List<NodeState> nodeStates;

  /** The parents of this {@code Node} in the {@link BayesianNetwork} structure */
  private List<Node> parents;

  /** The children of this {@code Node} in the {@link BayesianNetwork} structure */
  private List<Node> children;

  /**
   * Constructs a new {@code Node} from its identifier and the identifiers of its {@link NodeState}
   * values.
   *
   * @param nodeId the {@link Serializable} identifier of the {@code Node}.
   * @param stateIDs the {@link Serializable} identifiers of the {@link NodeState} values.
   * @throws NullPointerException if the node identifier is {@code null}.
   */
  public <T extends Serializable, E extends Serializable> Node(
      @NonNull T nodeId, Collection<E> stateIDs) {
    this.id = nodeId;
    this.parents = List.of();
    this.children = List.of();
    this.nodeStates = List.of();
    NodeUtils.addNodeStates(this, stateIDs);
  }

  /**
   * Constructs a new {@code Node} from its identifier
   *
   * @param nodeId the {@link Serializable} identifier of the {@code Node}.
   * @throws NullPointerException if the node identifier is {@code null}.
   */
  public <T extends Serializable> Node(@NonNull T nodeId) {
    this.id = nodeId;
    this.parents = List.of();
    this.children = List.of();
    this.nodeStates = List.of();
  }

  /**
   * Sets a list of {@link NodeState} values as the new active states for this {@code Node}. This
   * will fire a property change event with the label {@code "NODE_STATES_UPDATED"}. If validation
   * of the new list fails, the error will be logged and the previous {@link NodeState} list will be
   * restored.
   *
   * @param nodeStates the new list of {@link NodeState}s associated with this {@code Node}
   * @return {@code true} if the new list was successfully set, or {@code false} if an error was
   *     logged.
   */
  public boolean setNodeStates(List<NodeState> nodeStates) {
    List<NodeState> oldStates = this.nodeStates;
    this.nodeStates = Collections.unmodifiableList(nodeStates);
    try {
      support.firePropertyChange(NODE_STATES_UPDATED.name(), oldStates, nodeStates);
      return true;
    } catch (BayesNetIDException e) {
      log.error("Error setting states, '{}' reverting...", e.getMessage());
      this.nodeStates = oldStates;
      return false;
    }
  }

  /**
   * Sets a list of {@link Node}s as the new parents for this {@code Node}. This will fire the
   * property change event {@code "NODE_PARENTS_UPDATED"}. If validation of the new list fails, the
   * error will be logged and the previous parent list will be restored.
   *
   * @param parents the new list of parents connected to this {@code Node}.
   * @return {@code true} if the new list was successfully set, or {@code false} if an error was
   *     logged.
   */
  public boolean setParents(List<Node> parents) {
    List<Node> oldParents = this.parents;
    this.parents = Collections.unmodifiableList(parents);
    try {
      support.firePropertyChange(NODE_PARENTS_UPDATED.name(), oldParents, parents);
      return true;
    } catch (BayesNetIDException e) {
      log.error("Error setting parents, '{}' reverting...", e.getMessage());
      this.parents = oldParents;
      return false;
    }
  }

  /**
   * Sets a list of {@link Node}s as the new parents for this {@code Node}. This will fire the
   * property change event {@code "NODE_CHILDREN_UPDATED"}. If validation of the new list fails, the
   * error will be logged and the previous children list will be restored.
   *
   * @param children the new list of children connected to this {@code Node}.
   * @return {@code true} if the new list was successfully set, or {@code false} if an error was
   *     logged.
   */
  public boolean setChildren(List<Node> children) {
    List<Node> oldChildren = this.children;
    this.children = Collections.unmodifiableList(children);
    try {
      support.firePropertyChange(NODE_CHILDREN_UPDATED.name(), oldChildren, children);
      return true;
    } catch (BayesNetIDException e) {
      log.error("Error setting children, '{}' reverting...", e.getMessage());
      this.children = oldChildren;
      return false;
    }
  }

  public <T extends Serializable> NodeState addState(T stateID) {
    try {
      return NodeUtils.addNodeState(this, stateID);
    } catch (NoSuchElementException e) {
      return null;
    }
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

  /**
   * Adds this {@code Node} to a {@link PropertyChangeListener} and fires the property change event
   * {@code "NODE_ADDED_TO_NETWORK"}. This method is used internally by the implementation of {@link
   * BayesianNetwork}.
   *
   * @param listener a {@link PropertyChangeListener} instance.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
    support.firePropertyChange(NODE_ADDED_TO_NETWORK.name(), null, this);
  }

  /**
   * Removes this {@code Node} from a {@link PropertyChangeListener} and fires the property change
   * event {@code "NODE_REMOVED_FROM_NETWORK"}. This method is used internally by the implementation
   * of {@link BayesianNetwork}.
   *
   * @param listener a {@link PropertyChangeListener} instance listening to this {@code Node}.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.firePropertyChange(NODE_REMOVED_FROM_NETWORK.name(), this, null);
    support.removePropertyChangeListener(listener);
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
