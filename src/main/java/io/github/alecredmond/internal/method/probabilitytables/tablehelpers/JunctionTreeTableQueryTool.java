package io.github.alecredmond.internal.method.probabilitytables.tablehelpers;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.method.probabilitytables.TableQueryTool;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;
import java.util.Collection;
import java.util.Set;

public interface JunctionTreeTableQueryTool extends TableQueryTool {
  double sumProbabilities(Collection<NodeState> states);

  void setObserved(Set<NodeState> evidenceInTable);

  void resetObservations();

  ProbabilityVector getVector();

  JunctionTreeTable getTable();
}
