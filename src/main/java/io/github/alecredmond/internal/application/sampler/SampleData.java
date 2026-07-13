package io.github.alecredmond.internal.application.sampler;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.sampler.SampleUtils;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;

@Data
public class SampleData {
  private final NodeState[] rawStateArray;
  private final Set<NodeState> rawStateSet;
  private NodeState[] exportStateArray;
  private int count;

  public SampleData(NodeState[] rawStateArray) {
    this.rawStateArray = rawStateArray;
    this.exportStateArray = Arrays.copyOf(rawStateArray, rawStateArray.length);
    this.rawStateSet = SampleUtils.stateArrayToCollection(rawStateArray, LinkedHashSet::new);
    this.count = 0;
  }
}
