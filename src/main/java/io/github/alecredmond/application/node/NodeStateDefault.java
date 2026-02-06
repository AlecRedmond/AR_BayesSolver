package io.github.alecredmond.application.node;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NodeStateDefault implements NodeState {
  @EqualsAndHashCode.Include private final Object id;
  private final Node node;

  public <T> NodeStateDefault(T id, Node node) {
    this.id = id;
    this.node = node;
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
