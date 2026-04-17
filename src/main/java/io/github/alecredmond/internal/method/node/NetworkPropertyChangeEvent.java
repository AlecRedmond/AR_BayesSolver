package io.github.alecredmond.internal.method.node;

import io.github.alecredmond.internal.method.network.changehandlers.*;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public enum NetworkPropertyChangeEvent {
  NODE_STATES_UPDATED(NodeStateChangeHandler::new),
  NODE_PARENTS_UPDATED(NodeParentChangeHandler::new),
  NODE_CHILDREN_UPDATED(NodeChildChangeHandler::new),
  NODE_ADDED_TO_NETWORK(AddedNodeChangeHandler::new),
  NODE_REMOVED_FROM_NETWORK(RemovedNodeChangeHandler::new);

  private final Supplier<NetworkChangeHandler> supplier;

  NetworkPropertyChangeEvent(Supplier<NetworkChangeHandler> supplier) {
    this.supplier = supplier;
  }
}
