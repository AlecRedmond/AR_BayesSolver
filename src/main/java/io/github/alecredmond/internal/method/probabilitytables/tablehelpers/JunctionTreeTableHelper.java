package io.github.alecredmond.internal.method.probabilitytables.tablehelpers;

import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.method.probabilitytables.TableHelper;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTable;

import java.util.Map;

public interface JunctionTreeTableHelper extends TableHelper<JunctionTreeTable> {
    double sumProbabilities(Map<Node, NodeState> request);
}
