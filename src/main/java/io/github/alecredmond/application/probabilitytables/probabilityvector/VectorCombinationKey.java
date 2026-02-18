package io.github.alecredmond.application.probabilitytables.probabilityvector;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VectorCombinationKey {
  protected Map<Node, NodeState> request;
  protected int[] tumblerKey;
  protected boolean[] innerLock;
  protected boolean[] outerLock;
}
