package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ConditionalTable;
import java.util.Map;
import java.util.Set;

public interface ConditionalTableHelper extends NetworkTableHelper<ConditionalTable> {
  Map<Set<NodeState>, Double> buildProbabilitySetMap();
}
