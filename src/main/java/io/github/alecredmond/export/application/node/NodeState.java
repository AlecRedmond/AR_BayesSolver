package io.github.alecredmond.export.application.node;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NodeState {
  @EqualsAndHashCode.Include private final Serializable id;
  private final Node node;

  public <T extends Serializable> NodeState(@NonNull T id, @NonNull Node node) {
    this.id = id;
    this.node = node;
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
