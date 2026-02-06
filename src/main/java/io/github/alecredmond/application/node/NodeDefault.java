package io.github.alecredmond.application.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NodeDefault implements Node {
  @EqualsAndHashCode.Include private final Object id;
  private List<NodeState> nodeStates;
  private List<Node> parents;
  private List<Node> children;

  public <T, E> NodeDefault(T id, Collection<E> stateIDs) {
    this.id = id;
    this.parents = new ArrayList<>();
    this.children = new ArrayList<>();
    this.nodeStates = new ArrayList<>();
    stateIDs.forEach(this::addState);
  }

  public <T> NodeDefault(T id) {
    this.id = id;
    this.parents = new ArrayList<>();
    this.children = new ArrayList<>();
    this.nodeStates = new ArrayList<>();
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
