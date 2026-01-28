package io.github.alecredmond.application.probabilitytables;

import io.github.alecredmond.application.node.Node;
import io.github.alecredmond.application.node.NodeState;
import io.github.alecredmond.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.method.probabilitytables.ProbabilityVectorUtils;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
public class JunctionTreeTable extends ProbabilityTable {
  private final ProbabilityVector observedVector;
  private final Map<ProbabilityTable, Integer[]> indexPointerMap;
  private final Set<NodeState> observedStates;
  @Setter
  boolean observed;

  public <T> JunctionTreeTable(
      T tableID,
      ProbabilityVector vector,
      ProbabilityVectorUtils utils,
      Set<Node> events,
      ProbabilityVector observedVector,
      Map<ProbabilityTable, Integer[]> indexPointerMap,
      Map<Object, NodeState> nodeStateIDMap,
      Map<Object, Node> nodeIDMap) {
    super(nodeStateIDMap, nodeIDMap, vector, utils, tableID, events, events, Set.of());
    this.observedVector = observedVector;
    this.indexPointerMap = indexPointerMap;
    observedStates = new HashSet<>();
    observed = false;
  }

}
