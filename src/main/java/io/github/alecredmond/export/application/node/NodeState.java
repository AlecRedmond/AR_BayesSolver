package io.github.alecredmond.export.application.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NodeState{
  @EqualsAndHashCode.Include private final Serializable id;
  private final Node node;

  public <T extends Serializable> NodeState(T id, Node node) {
    this.id = id;
    this.node = node;
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
