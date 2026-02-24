package io.github.alecredmond.application.node;

import io.github.alecredmond.method.node.NodeUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Node {
  @EqualsAndHashCode.Include private final Object id;
  private List<NodeState> nodeStates;
  private List<Node> parents;
  private List<Node> children;

  public <T, E> Node(T id, Collection<E> stateIDs) {
    this.id = id;
    this.parents = new ArrayList<>();
    this.children = new ArrayList<>();
    this.nodeStates = new ArrayList<>();
    stateIDs.forEach(this::addState);
  }

  public <T> NodeState addState(T stateID) {
    return NodeUtils.addNodeState(this, stateID);
  }

  public <T> Node(T id) {
    this.id = id;
    this.parents = new ArrayList<>();
    this.children = new ArrayList<>();
    this.nodeStates = new ArrayList<>();
  }

  public void addParent(Node parent) {
    NodeUtils.addParent(this, parent);
  }

  public void removeParent(Node parent) {
    NodeUtils.removeParent(this, parent);
  }

  public <E> void removeState(E stateID) {
    NodeUtils.removeState(this, stateID);
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
