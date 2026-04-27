package io.github.alecredmond.internal.application.inference.junctiontree;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.MarginalTable;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.ConstraintHandler;
import io.github.alecredmond.internal.method.inference.junctiontree.handlers.JTAConstraintHandler;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JunctionTreeData {
  private BayesianNetworkData networkData;
  private Clique[] cliques;
  private Separator[] separators;
  private Map<Clique, List<ConstraintHandler>> constraintHandlersMap;
  private Map<Node, MarginalTable> observedTablesMap;
  private Map<Node, NodeState> observedEvidence;
  private boolean solverConfig;
  private double jointProbability;
}
