package io.github.alecredmond.internal.method.probabilitytables.tablehelpers;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.export.probabilitytables.ProbabilityTableQueryTool;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import java.util.Collection;
import java.util.Set;

public interface JunctionTreeTableQueryTool extends ProbabilityTableQueryTool {
  double sumProbabilities(Collection<NodeState> states);

  void setObserved(Set<NodeState> evidenceInTable);

  void resetObservations();

  ProbabilityVector getVector();

  JunctionTreeTable getTable();
}
