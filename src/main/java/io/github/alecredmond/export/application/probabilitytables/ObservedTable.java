package io.github.alecredmond.export.application.probabilitytables;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.probabilitytables.ObservedTableHelper;
import java.util.Map;

public interface ObservedTable extends ProbabilityTable {
  Node getNode();

  Map<Node, NodeState> getObservations();

  @Override
  ObservedTableHelper getHelper();
}
