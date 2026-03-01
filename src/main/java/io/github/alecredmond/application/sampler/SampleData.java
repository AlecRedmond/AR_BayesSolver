package io.github.alecredmond.application.sampler;

import io.github.alecredmond.application.node.NodeState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class SampleData {
  private final NodeState[] rawArray;
  private NodeState[] exportArray;
  private Collection<NodeState> stateCollection;
  private Supplier<? extends Collection<NodeState>> supplier;

  public SampleData(NodeState[] rawArray) {
    this.rawArray = rawArray;
    this.exportArray = Arrays.copyOf(rawArray, rawArray.length);
    this.supplier = ArrayList::new;
    this.stateCollection = Arrays.stream(exportArray).collect(Collectors.toCollection(supplier));
  }
}
