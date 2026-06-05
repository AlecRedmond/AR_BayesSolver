package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.base.ProbabilityTableBase;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilderData;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.JunctionTreeTableHelper;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JunctionTreeTableImpl
    extends ProbabilityTableBase<JunctionTreeTable, JunctionTreeTableHelper>
    implements JunctionTreeTable {
  @EqualsAndHashCode.Exclude private final ProbabilityVector backupVector;
  @EqualsAndHashCode.Exclude private final Set<NodeState> observedStates;

  public JunctionTreeTableImpl(
      TableBuilderData tableBuilderData,
      ProbabilityVector backupVector,
      Set<NodeState> observedStates) {
    super(tableBuilderData);
    this.backupVector = backupVector;
    this.observedStates = observedStates;
  }
}
