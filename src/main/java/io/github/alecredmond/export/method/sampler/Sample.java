package io.github.alecredmond.export.method.sampler;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.util.Collection;
import java.util.function.Supplier;

public interface Sample {
  static int countAll(Collection<Sample> samples) {
    return samples.stream().mapToInt(Sample::count).sum();
  }

  int count();

  NodeState[] getAllStates();

  NodeState[] getDisplayedStates();

  <T extends Collection<NodeState>, S extends T> T getDisplayedStates(Supplier<S> supplier);

  void displayAllNodes();

  void setDisplayedNodes(Collection<Node> nodes);

  boolean containsAll(Collection<NodeState> states);

  String toString();
}
