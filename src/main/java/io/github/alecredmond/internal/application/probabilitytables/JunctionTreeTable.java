package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.tablehelpers.JunctionTreeTableHelper;
import java.util.Set;

public interface JunctionTreeTable extends ProbabilityTable {
  ProbabilityVector getBackupVector();

  Set<NodeState> getObservedStates();

  JunctionTreeTableHelper getHelper();
}
