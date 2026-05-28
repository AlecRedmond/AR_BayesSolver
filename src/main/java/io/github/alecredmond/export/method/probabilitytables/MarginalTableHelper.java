package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import java.io.Serializable;
import java.util.Map;

public interface MarginalTableHelper extends TableHelper<MarginalTable> {
  Double getProbability(NodeState state);

  Double getProbabilityById(Serializable id);

  boolean setProbability(NodeState state, double probability);

  boolean setProbabilityById(Serializable id, double probability);

  Map<NodeState, Double> buildProbabilityMap();
}
