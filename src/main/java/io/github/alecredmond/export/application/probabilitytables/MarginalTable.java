package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class MarginalTable extends ProbabilityTable {
  private final Node networkNode;

  public MarginalTable(
      ProbabilityVector vector,
      String tableID,
      Node networkNode,
      Map<Object, NodeState> nodeStateIDMap,
      Map<Object, Node> nodeIDMap) {
    super(
        nodeStateIDMap,
        nodeIDMap,
        vector,
        tableID,
        Set.of(networkNode),
        Set.of(networkNode),
        new HashSet<>());
    this.networkNode = networkNode;
  }

  public <T> double getProbability(T nodeStateID) {
    return super.getProbabilityFromIDs(Set.of(nodeStateID));
  }

  @Override
  public void marginalizeTable() {
    TableUtils.marginalizeJointTable(this);
  }
}
