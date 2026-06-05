package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.probabilityvector.ProbabilityVector;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface ProbabilityTable {
  Map<Serializable, NodeState> getNodeStateIDMap();

  Map<Serializable, Node> getNodeIDMap();

  ProbabilityVector getVector();

  Set<Node> getNodes();

  Set<Node> getEvents();

  Set<Node> getConditions();

  Serializable getTableName();

  @SuppressWarnings("rawtypes")
  TableHelper getHelper();

  double[] getProbabilities();
}
