package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.JunctionTreeTableHelper;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.JunctionTreeTableHelperImpl;
import java.io.Serializable;
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

  public <T extends Serializable> JunctionTreeTable(
      T tableID,
      ProbabilityVector vector,
      Set<Node> events,
      ProbabilityVector backupVector,
      Map<Serializable, NodeState> nodeStateIDMap,
      Map<Serializable, Node> nodeIDMap) {
    super(nodeStateIDMap, nodeIDMap, vector, tableID, events, events, Set.of());
    this.backupVector = backupVector;
    this.observedStates = new HashSet<>();
    ((JunctionTreeTableHelperImpl) getHelper()).initHelper();
  }

  @Override
  public JunctionTreeTableHelper getHelper() {
    return (JunctionTreeTableHelperImpl) helper;
  }

  public void marginalizeTable() {
    TableUtils.marginalizeJointTable(this);
  }

  @Override
  protected TableHelper<JunctionTreeTable> buildHelper() {
    return new JunctionTreeTableHelperImpl(this);
  }
}
