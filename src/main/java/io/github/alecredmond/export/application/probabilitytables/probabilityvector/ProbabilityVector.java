package io.github.alecredmond.export.application.probabilitytables.probabilityvector;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@Data
@AllArgsConstructor
public class ProbabilityVector {
  private final Node[] nodeArray;
  private final int[] numberOfStates;
  private final int[] stepMultiplier;
  @Exclude private final double[] probabilities;
  private final Map<Node, Integer> nodeIndexMap;
  private final Map<NodeState, Integer> stateValueMap;
}
