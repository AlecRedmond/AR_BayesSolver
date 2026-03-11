package io.github.alecredmond.application.probabilitytables.internal.probabilityvector;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VectorCombinationKey {
  private Map<Node, NodeState> request;
  private int[] stateIndexes;
  private boolean[] iterateEvents;
  private boolean[] iterateConditions;

  public boolean[] getIterateCommon() {
    return iterateConditions;
  }

  public boolean[] getIterateExclusive() {
    return iterateEvents;
  }
}
