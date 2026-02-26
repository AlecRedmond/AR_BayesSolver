package io.github.alecredmond.application.sampler;

import io.github.alecredmond.application.node.NodeState;
import java.util.Collection;
import lombok.Data;

@Data
public class Sample {
  private final NodeState[] rawSample;
  private Collection<NodeState> exportSample;

  public Sample(NodeState[] rawSample){
      this.rawSample = rawSample;
  }
}
