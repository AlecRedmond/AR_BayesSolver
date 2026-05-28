package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ConditionalTableHelper extends TableHelper<ConditionalTable> {
  boolean setProbability(Collection<NodeState> states, double probability);

  boolean setProbabilityById(Collection<Serializable> stateIds, double probability);

  Map<Set<NodeState>, Double> buildProbabilitySetMap();
}
