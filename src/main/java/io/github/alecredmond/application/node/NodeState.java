package io.github.alecredmond.application.node;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NodeState {
  @EqualsAndHashCode.Include private final Object id;
  private final Node node;

  public <T> NodeState(T id, Node node) {
    this.id = id;
    this.node = node;
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
