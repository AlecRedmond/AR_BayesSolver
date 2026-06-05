package io.github.alecredmond.export.method.probabilitytables;

import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.RootNodeTable;

import java.io.Serializable;
import java.util.Map;

public interface RootNodeTableHelper extends NetworkTableHelper<RootNodeTable> {
    Double getProbability(NodeState state);

    Double getProbabilityById(Serializable id);

    Map<NodeState,Double> buildProbabilityMap();
}
