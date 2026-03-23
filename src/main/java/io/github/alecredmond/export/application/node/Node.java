package io.github.alecredmond.export.application.node;

import io.github.alecredmond.internal.method.node.NodeUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Node {
  @EqualsAndHashCode.Include
  private final Serializable id;
  private List<NodeState> nodeStates;
  private List<Node> parents;
  private List<Node> children;

  public <T extends Serializable, E extends Serializable> Node(T id, Collection<E> stateIDs) {
    this.id = id;
    this.parents = new ArrayList<>();
    this.children = new ArrayList<>();
    this.nodeStates = new ArrayList<>();
    stateIDs.forEach(this::addState);
  }

  public <T extends Serializable> NodeState addState(T stateID) {
    return NodeUtils.addNodeState(this, stateID);
  }

  public <T extends Serializable> Node(T id) {
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

  public <E extends Serializable> void removeState(E stateID) {
    NodeUtils.removeState(this, stateID);
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
