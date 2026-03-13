package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class JunctionTreeTable extends ProbabilityTable {
  private final ProbabilityVector backupVector;
  private final Set<NodeState> observedStates;

  public <T> JunctionTreeTable(
      T tableID,
      ProbabilityVector vector,
      Set<Node> events,
      ProbabilityVector backupVector,
      Map<Object, NodeState> nodeStateIDMap,
      Map<Object, Node> nodeIDMap) {
    super(nodeStateIDMap, nodeIDMap, vector, tableID, events, events, Set.of());
    this.backupVector = backupVector;
    observedStates = new HashSet<>();
  }

  @Override
  public void marginalizeTable() {
    TableUtils.marginalizeJointTable(this);
  }
}
