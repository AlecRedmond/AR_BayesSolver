package io.github.alecredmond.internal.application.sampler;

import io.github.alecredmond.export.application.node.NodeState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class SampleData {
  private final NodeState[] rawStateArray;
  private NodeState[] exportStateArray;
  private int count;
  private Collection<NodeState> stateCollection;
  private Supplier<? extends Collection<NodeState>> supplier;

  public SampleData(NodeState[] rawStateArray) {
    this.rawStateArray = rawStateArray;
    this.exportStateArray = Arrays.copyOf(rawStateArray, rawStateArray.length);
    this.supplier = ArrayList::new;
    this.stateCollection =
        Arrays.stream(exportStateArray).collect(Collectors.toCollection(supplier));
    this.count = 0;
  }
}
