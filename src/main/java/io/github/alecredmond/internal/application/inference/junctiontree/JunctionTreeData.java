package io.github.alecredmond.internal.application.inference.junctiontree;

import io.github.alecredmond.export.application.network.BayesianNetworkData;
import io.github.alecredmond.export.application.node.Node;
import io.github.alecredmond.export.application.node.NodeState;
import io.github.alecredmond.export.application.probabilitytables.ObservedTable;
import io.github.alecredmond.export.method.inference.SolverAlgorithm;
import io.github.alecredmond.export.method.inference.InferenceAlgorithm;
import io.github.alecredmond.internal.method.constraints.strategies.ConstraintSolver;
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
  private Map<Clique, List<ConstraintSolver>> solversPerClique;
  private Map<Node, ObservedTable> observedTablesMap;
  private Map<Node, NodeState> observedEvidence;
  private boolean solverConfig;
  private double jointProbability;
  private SolverAlgorithm solverAlgorithm;
  private InferenceAlgorithm inferenceAlgorithm;
  private double equivalentTreeWidth;
}
