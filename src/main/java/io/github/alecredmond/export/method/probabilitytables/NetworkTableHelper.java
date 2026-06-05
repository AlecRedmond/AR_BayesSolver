package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.NetworkTable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface NetworkTableHelper<T extends NetworkTable> extends TableHelper<T> {
  Map<NodeState, Double> getConditionalProb(Collection<NodeState> condition);

  Map<NodeState, Double> getConditionalProbByIds(Collection<Serializable> conditionIDs);
}
