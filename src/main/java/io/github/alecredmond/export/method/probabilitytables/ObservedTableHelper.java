package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ObservedTable;
import java.io.Serializable;
import java.util.Map;

public interface ObservedTableHelper extends TableHelper<ObservedTable> {
  Double getProbability(NodeState state);

  Double getProbabilityById(Serializable id);

  Map<NodeState, Double> buildProbabilityMap();
}
