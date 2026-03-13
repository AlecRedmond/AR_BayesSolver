package io.github.alecredmond.internal.application.probabilitytables.probabilityvector;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
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
}
