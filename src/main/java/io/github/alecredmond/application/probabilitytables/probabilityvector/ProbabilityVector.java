package io.github.alecredmond.application.probabilitytables.probabilityvector;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProbabilityVector {
  private final Node[] nodeArray;
  private final int[] numberOfStates;
  private final int[] stepMultiplier;
  private final double[] probabilities;
  private final Map<Node, Integer> nodeIndexMap;
  private final Map<NodeState, Integer> stateValueMap;
}
