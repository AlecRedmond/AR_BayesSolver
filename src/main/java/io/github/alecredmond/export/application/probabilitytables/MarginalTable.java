package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import java.io.Serializable;
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
      Serializable tableName,
      Node networkNode,
      Map<Serializable, NodeState> nodeStateIDMap,
      Map<Serializable, Node> nodeIDMap) {
    super(
        nodeStateIDMap,
        nodeIDMap,
        vector,
        tableName,
        Set.of(networkNode),
        Set.of(networkNode),
        new HashSet<>());
    this.networkNode = networkNode;
  }

  public <T extends Serializable> double getProbability(T nodeStateID) {
    return super.getProbabilityFromIDs(Set.of(nodeStateID));
  }

  @Override
  public void marginalizeTable() {
    TableUtils.marginalizeJointTable(this);
  }
}
