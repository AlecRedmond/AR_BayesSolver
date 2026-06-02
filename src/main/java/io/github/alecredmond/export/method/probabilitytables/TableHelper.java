package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ProbabilityTable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface TableHelper<T extends ProbabilityTable> {
  Double getProbability(Collection<NodeState> states);

  Double getProbabilityFromIDs(Collection<Serializable> stateIds);

  T copyTable();

  void marginalizeTable();

  Map<NodeState, Double> getConditionalProb(Collection<NodeState> condition);

  Map<NodeState, Double> getConditionalProbByIds(Collection<Serializable> conditionIDs);

  void setSafeMode(boolean safeMode);
}
